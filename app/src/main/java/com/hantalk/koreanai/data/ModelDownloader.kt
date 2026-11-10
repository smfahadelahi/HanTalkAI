package com.hantalk.koreanai.data

import android.content.Context
import android.util.Log
import com.hantalk.koreanai.llm.ModelSpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

sealed interface DownloadEvent {
    data class Progress(val bytes: Long, val total: Long, val resuming: Boolean) : DownloadEvent {
        val percent: Int
            get() = if (total > 0) ((bytes * 100) / total).toInt().coerceIn(0, 100) else 0
    }

    data class Done(val file: File) : DownloadEvent
    data class Failed(val message: String) : DownloadEvent
}

/**
 * Resumable HTTP downloader for model files.
 *
 * A `.part` file is kept next to the final file, so a dropped connection, a
 * killed process or a flaky network only costs the bytes still missing.
 */
class ModelDownloader(private val context: Context) {

    fun download(spec: ModelSpec): Flow<DownloadEvent> = flow {
        val target = ModelStore.modelFile(context, spec)
        if (ModelStore.isInstalled(context, spec)) {
            emit(DownloadEvent.Done(target))
            return@flow
        }

        val part = ModelStore.partialFile(context, spec)
        var existing = if (part.exists()) part.length() else 0L
        val resuming = existing > 0L

        val connection = openConnection(spec.url, existing)
        try {
            val responseCode = connection.responseCode

            if (responseCode == HttpURLConnection.HTTP_OK && existing > 0L) {
                // Server ignored the Range request — start over.
                existing = 0L
                part.delete()
            } else if (responseCode != HttpURLConnection.HTTP_OK && responseCode != 206) {
                emit(DownloadEvent.Failed("Server returned HTTP $responseCode"))
                return@flow
            }

            val contentLength = connection.contentLengthLong
            val total = if (contentLength > 0) contentLength + existing else spec.approxSizeBytes

            var downloaded = existing
            emit(DownloadEvent.Progress(downloaded, total, resuming))

            var lastEmit = 0L
            connection.inputStream.use { input ->
                part.appendBuffered(existing > 0L).use { output ->
                    val buffer = ByteArray(BUFFER_SIZE)
                    while (true) {
                        val read = input.read(buffer)
                        if (read == -1) break
                        output.write(buffer, 0, read)
                        downloaded += read
                        val now = System.currentTimeMillis()
                        if (now - lastEmit > 120) {
                            lastEmit = now
                            emit(DownloadEvent.Progress(downloaded, total, resuming))
                        }
                    }
                    output.flush()
                }
            }

            if (!part.renameTo(target)) {
                part.copyTo(target, overwrite = true)
                part.delete()
            }

            emit(DownloadEvent.Progress(target.length(), target.length(), resuming))
            emit(DownloadEvent.Done(target))
        } catch (e: IOException) {
            Log.w(TAG, "Download interrupted: ${e.message}")
            emit(DownloadEvent.Failed(e.message ?: "Network error while downloading"))
        } finally {
            runCatching { connection.disconnect() }
        }
    }.flowOn(Dispatchers.IO)

    private fun openConnection(url: String, offset: Long): HttpURLConnection {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 30_000
            readTimeout = 30_000
            instanceFollowRedirects = true
            setRequestProperty("User-Agent", "HanTalkAI/1.0 (Android)")
            setRequestProperty("Accept", "*/*")
            if (offset > 0L) {
                setRequestProperty("Range", "bytes=$offset-")
            }
        }
        connection.connect()
        return connection
    }

    private fun File.appendBuffered(append: Boolean) =
        java.io.BufferedOutputStream(java.io.FileOutputStream(this, append), BUFFER_SIZE)

    companion object {
        private const val TAG = "ModelDownloader"
        private const val BUFFER_SIZE = 128 * 1024
    }
}
