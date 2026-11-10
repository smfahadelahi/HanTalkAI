package com.hantalk.koreanai.llm

import java.util.Locale

/**
 * A model the app can load into MediaPipe's LLM Inference engine.
 *
 * [url] is an HTTPS link to a `.task`/`.bin` file. Models are hundreds of MB, so
 * they are never bundled in the APK — they are downloaded on demand by the user.
 */
data class ModelSpec(
    val id: String,
    val displayName: String,
    val fileName: String,
    val approxSizeMb: Int,
    val minRamGb: Double,
    val url: String,
    val note: String
) {
    val approxSizeBytes: Long get() = approxSizeMb.toLong() * 1024L * 1024L
}

object ModelRegistry {

    /** Entry shown first: the most likely to run on a mid-range phone (4 GB+). */
    val gemma3_1B = ModelSpec(
        id = "gemma3-1b-it",
        displayName = "Gemma 3 1B (int4)",
        fileName = "gemma3-1b-it-int4.task",
        approxSizeMb = 550,
        minRamGb = 4.0,
        url = "https://huggingface.co/litert-community/Gemma3-1B-IT/resolve/main/Gemma3-1B-IT_multi-prefill-seq_q4_ekv2048.task",
        note = "Smallest Gemma — best balance of speed and quality on 4 GB phones."
    )

    val gemma2_2B = ModelSpec(
        id = "gemma2-2b-it",
        displayName = "Gemma 2 2B (int8)",
        fileName = "gemma2-2b-it-int8.task",
        approxSizeMb = 2700,
        minRamGb = 8.0,
        url = "https://huggingface.co/google/gemma-2-2b-it/resolve/main/gemma2-2b-it-gpu-int8.task",
        note = "Better Korean, but needs a flagship-class phone (8 GB+)."
    )

    val qwen2_1_5B = ModelSpec(
        id = "qwen2.5-1.5b-it",
        displayName = "Qwen 2.5 1.5B (int8)",
        fileName = "qwen2.5-1.5b-instruct-int8.task",
        approxSizeMb = 1600,
        minRamGb = 6.0,
        url = "https://huggingface.co/litert-community/Qwen2.5-1.5B-Instruct/resolve/main/Qwen2.5-1.5B-Instruct_multi-prefill-seq_q8_ekv1280.task",
        note = "Strong Korean for a small model; needs about 6 GB of RAM."
    )

    val all: List<ModelSpec> = listOf(gemma3_1B, gemma2_2B, qwen2_1_5B)

    fun byId(id: String): ModelSpec? = all.firstOrNull { it.id == id }

    /** Guesses how much RAM the device really has, in GB, rounded down. */
    fun deviceRamGb(totalMemBytes: Long): Double =
        (totalMemBytes.toDouble() / (1024.0 * 1024.0 * 1024.0)).let {
            (it * 10).toLong() / 10.0
        }

    /** True when the device is likely to fit [spec] in memory. */
    fun canRun(spec: ModelSpec, totalMemBytes: Long): Boolean {
        val ram = deviceRamGb(totalMemBytes)
        // Leave headroom: the model, its KV cache and the app all share memory.
        return ram >= spec.minRamGb
    }

    fun formatSize(mb: Int): String = when {
        mb >= 1024 -> String.format(Locale.US, "%.1f GB", mb / 1024.0)
        else -> "$mb MB"
    }
}
