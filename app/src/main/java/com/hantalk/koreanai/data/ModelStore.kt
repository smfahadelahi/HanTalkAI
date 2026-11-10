package com.hantalk.koreanai.data

import android.content.Context
import com.hantalk.koreanai.llm.ModelSpec
import java.io.File

/**
 * Models live in the app-specific external files dir when available (plenty of
 * room, not counted against the APK) and fall back to internal storage.
 */
object ModelStore {

    fun modelsDir(context: Context): File {
        val external = context.getExternalFilesDir("models")
        val dir = external ?: File(context.filesDir, "models")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun modelFile(context: Context, spec: ModelSpec): File =
        File(modelsDir(context), spec.fileName)

    fun partialFile(context: Context, spec: ModelSpec): File =
        File(modelsDir(context), "${spec.fileName}.part")

    fun isInstalled(context: Context, spec: ModelSpec): Boolean {
        val f = modelFile(context, spec)
        return f.exists() && f.length() > 50L * 1024L * 1024L
    }

    fun installedModels(context: Context): List<ModelSpec> =
        com.hantalk.koreanai.llm.ModelRegistry.all.filter { isInstalled(context, it) }

    fun delete(context: Context, spec: ModelSpec): Boolean {
        val ok1 = modelFile(context, spec).delete()
        partialFile(context, spec).delete()
        return ok1
    }

    /** Bytes already on disk for [spec] (complete file, or partial download). */
    fun bytesOnDisk(context: Context, spec: ModelSpec): Long {
        val done = modelFile(context, spec)
        if (done.exists()) return done.length()
        val part = partialFile(context, spec)
        return if (part.exists()) part.length() else 0L
    }
}
