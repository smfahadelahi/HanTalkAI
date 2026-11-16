package com.hantalk.koreanai.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hantalk.koreanai.data.DownloadEvent
import com.hantalk.koreanai.data.ModelDownloader
import com.hantalk.koreanai.data.ModelStore
import com.hantalk.koreanai.data.Prefs
import com.hantalk.koreanai.domain.Curriculum
import com.hantalk.koreanai.domain.Lesson
import com.hantalk.koreanai.domain.Phrase
import com.hantalk.koreanai.domain.hangul.Hangul
import com.hantalk.koreanai.domain.text.Similarity
import com.hantalk.koreanai.llm.EngineStatus
import com.hantalk.koreanai.llm.LlmEngine
import com.hantalk.koreanai.llm.LocalTutor
import com.hantalk.koreanai.llm.ModelRegistry
import com.hantalk.koreanai.llm.ModelSpec
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatMessage(
    val id: Long,
    val text: String,
    val fromUser: Boolean,
    val streaming: Boolean = false,
    /** True when this reply was created in voice mode and should be read out. */
    val spoken: Boolean = false
)

data class ModelUiState(
    val spec: ModelSpec,
    val installed: Boolean,
    val canRun: Boolean,
    val downloading: Boolean = false,
    val percent: Int = 0,
    val bytes: Long = 0,
    val total: Long = 0,
    val error: String? = null
)

data class PracticeState(
    val phrase: Phrase? = null,
    val attempt: String = "",
    val score: Double? = null,
    val feedback: String = "",
    val listening: Boolean = false
)

class HanTalkViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = Prefs(app)
    private val downloader = ModelDownloader(app)
    private val engine = LlmEngine(app)

    // ---------- Chat ----------
    private val _messages = MutableStateFlow(
        listOf(
            ChatMessage(
                id = 0,
                text = "안녕하세요! I'm HanTalk, your Korean tutor.\n\n" +
                    "Ask me about 한글, grammar, numbers — or just say hello in Korean.",
                fromUser = false
            )
        )
    )
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _chatBusy = MutableStateFlow(false)
    val chatBusy: StateFlow<Boolean> = _chatBusy.asStateFlow()

    private val _engineStatus = MutableStateFlow<EngineStatus>(EngineStatus.NoModel)
    val engineStatus: StateFlow<EngineStatus> = _engineStatus.asStateFlow()

    private var chatJob: Job? = null
    private var nextId = 1L

    // ---------- Voice mode ----------
    /** Replies are read aloud automatically while voice mode is on. */
    private val _autoSpeak = MutableStateFlow(prefs.autoSpeak)
    val autoSpeak: StateFlow<Boolean> = _autoSpeak.asStateFlow()

    /** The Korean sentence the tutor most recently said — shown as a "say this". */
    private val _sayThis = MutableStateFlow<Phrase?>(null)
    val sayThis: StateFlow<Phrase?> = _sayThis.asStateFlow()

    /** True while a spoken question is being turned into a chat message. */
    private val _voiceThinking = MutableStateFlow(false)
    val voiceThinking: StateFlow<Boolean> = _voiceThinking.asStateFlow()

    fun toggleAutoSpeak() {
        _autoSpeak.value = !_autoSpeak.value
        prefs.autoSpeak = _autoSpeak.value
    }

    /**
     * Sends a question that came in through the microphone. The answer is marked
     * as [ChatMessage.spoken] so the voice screen reads it back automatically.
     */
    fun sendSpokenMessage(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty() || _chatBusy.value) return
        _voiceThinking.value = true
        sendMessage(trimmed, spoken = true, onFirstReply = { _voiceThinking.value = false })
    }

    /**
     * Pulls out the part of a tutor reply that is worth reading aloud: Korean
     * sentences only, with markdown, romanisation and tables removed so TTS
     * never spells out "asterisk asterisk".
     */
    fun speakableReply(raw: String): String {
        val koreanOnly = raw
            .lineSequence()
            .filterNot { it.trimStart().startsWith("|") }
            .joinToString("\n")
            .replace(Regex("\\*\\*(.+?)\\*\\*"), "$1")
            .replace(Regex("\\*(.+?)\\*"), "$1")
            .replace(Regex("\\[(.*?)]\\((.*?)\\)"), "$1")
            .replace(Regex("(?m)^\\s*[•\\-\\*]\\s*"), "")
            .replace("\u2014", ",")

        val sentences = koreanOnly
            .split(Regex("(?<=[.!?다요])\\s+|\\n+"))
            .map { it.trim() }
            .filter { segment ->
                val hangul = segment.count { it in '\uAC00'..'\uD7A3' }
                hangul >= 2 && hangul * 2 >= segment.replace(" ", "").length
            }
        return sentences.joinToString(" ").take(300)
    }

    /** Suggests a fresh phrase for voice practice. */
    fun nextSayThis() {
        _sayThis.value = Curriculum.allPhrases.random()
    }

    // ---------- Models ----------
    private val _models = MutableStateFlow<List<ModelUiState>>(emptyList())
    val models: StateFlow<List<ModelUiState>> = _models.asStateFlow()

    private val deviceRamBytes: Long = readTotalRam()

    // ---------- Progress ----------
    private val _stats = MutableStateFlow(Stats())
    val stats: StateFlow<Stats> = _stats.asStateFlow()

    data class Stats(
        val practiceCount: Int = 0,
        val bestScore: Int = 0,
        val streak: Int = 0,
        val learnedLetters: Int = 0,
        val totalLetters: Int = Curriculum.consonants.size + Curriculum.vowels.size
    )

    // ---------- Practice ----------
    private val _practice = MutableStateFlow(PracticeState())
    val practice: StateFlow<PracticeState> = _practice.asStateFlow()

    // ---------- Alphabet ----------
    private val _selectedLetter = MutableStateFlow<String?>(null)
    val selectedLetter: StateFlow<String?> = _selectedLetter.asStateFlow()

    init {
        _sayThis.value = Curriculum.allPhrases.random()
        refreshModels()
        refreshStats()
        restoreModel()
    }

    // ================= Chat =================

    fun sendMessage(
        text: String,
        spoken: Boolean = false,
        onFirstReply: (() -> Unit)? = null
    ) {
        val trimmed = text.trim()
        if (trimmed.isEmpty() || _chatBusy.value) return

        val userMessage = ChatMessage(nextId++, trimmed, fromUser = true)
        val replyId = nextId++
        _messages.update {
            it + userMessage + ChatMessage(
                id = replyId,
                text = "",
                fromUser = false,
                streaming = true,
                spoken = spoken
            )
        }
        _chatBusy.value = true

        chatJob?.cancel()
        chatJob = viewModelScope.launch {
            try {
                var announced = false
                fun announceOnce() {
                    if (!announced) {
                        announced = true
                        _voiceThinking.value = false
                        onFirstReply?.invoke()
                    }
                }
                engine.streamReply(trimmed)
                    .catch { throwable ->
                        setMessage(replyId, LocalTutor.reply(trimmed), streaming = false)
                    }
                    .collect { partial ->
                        if (partial.isNotBlank()) announceOnce()
                        setMessage(replyId, partial, streaming = true)
                    }
                val finished = currentText(replyId)
                if (finished.isBlank()) {
                    // The model streamed nothing (or was stopped early): never
                    // leave an empty bubble behind.
                    setMessage(
                        replyId,
                        "Sorry — I didn't manage to answer that. Try asking in a " +
                            "different way, or rephrase it in Korean.",
                        streaming = false
                    )
                } else {
                    setMessage(replyId, finished, streaming = false)
                }
                announceOnce()
            } catch (t: Throwable) {
                _voiceThinking.value = false
                setMessage(
                    replyId,
                    "Something went wrong, so here is the offline answer:\n\n" +
                        LocalTutor.reply(trimmed),
                    streaming = false
                )
            } finally {
                _chatBusy.value = false
            }
        }
    }

    fun stopGenerating() {
        chatJob?.cancel()
        _chatBusy.value = false
        _messages.update { list ->
            list.map { if (it.streaming) it.copy(streaming = false) else it }
        }
    }

    fun clearChat() {
        chatJob?.cancel()
        _chatBusy.value = false
        _messages.value = listOf(
            ChatMessage(
                id = nextId++,
                text = "새로 시작해요! Let's start again. What would you like to learn?",
                fromUser = false
            )
        )
    }

    private fun currentText(id: Long): String =
        _messages.value.firstOrNull { it.id == id }?.text.orEmpty()

    private fun setMessage(id: Long, text: String, streaming: Boolean) {
        _messages.update { list ->
            list.map { if (it.id == id) it.copy(text = text, streaming = streaming) else it }
        }
    }

    // ================= Models =================

    fun refreshModels() {
        val app = getApplication<Application>()
        _models.value = ModelRegistry.all.map { spec ->
            ModelUiState(
                spec = spec,
                installed = ModelStore.isInstalled(app, spec),
                canRun = ModelRegistry.canRun(spec, deviceRamBytes)
            )
        }
    }

    private fun restoreModel() {
        val id = prefs.activeModelId ?: return
        val spec = ModelRegistry.byId(id) ?: return
        if (!ModelStore.isInstalled(getApplication(), spec)) return
        viewModelScope.launch {
            engine.load(spec)
            _engineStatus.value = engine.status
        }
    }

    fun downloadModel(spec: ModelSpec) {
        val current = _models.value.firstOrNull { it.spec.id == spec.id }
        if (current?.downloading == true) return

        updateModel(spec.id) { it.copy(downloading = true, percent = 0, error = null) }

        viewModelScope.launch {
            downloader.download(spec)
                .catch { throwable ->
                    updateModel(spec.id) {
                        it.copy(
                            downloading = false,
                            error = throwable.message ?: "Download failed"
                        )
                    }
                }
                .collect { event ->
                    when (event) {
                        is DownloadEvent.Progress -> updateModel(spec.id) {
                            it.copy(
                                percent = event.percent,
                                bytes = event.bytes,
                                total = event.total
                            )
                        }

                        is DownloadEvent.Done -> {
                            updateModel(spec.id) { it.copy(downloading = false, percent = 100) }
                            activateModel(spec)
                        }

                        is DownloadEvent.Failed -> updateModel(spec.id) {
                            it.copy(downloading = false, error = event.message)
                        }
                    }
                }
            refreshModels()
        }
    }

    fun cancelDownload(spec: ModelSpec) {
        updateModel(spec.id) { it.copy(downloading = false) }
    }

    fun activateModel(spec: ModelSpec) {
        viewModelScope.launch {
            val result = engine.load(spec)
            _engineStatus.value = engine.status
            if (result.isSuccess) {
                prefs.activeModelId = spec.id
                addSystemNote("${spec.displayName} is ready. Ask me anything — it all runs on your phone.")
            } else {
                prefs.activeModelId = null
                addSystemNote(
                    "Could not start ${spec.displayName}. " +
                        (engine.status as? EngineStatus.Failed)?.message.orEmpty() +
                        "\nThe built-in offline tutor still works."
                )
            }
        }
    }

    fun deleteModel(spec: ModelSpec) {
        viewModelScope.launch {
            if (prefs.activeModelId == spec.id) {
                engine.release()
                prefs.activeModelId = null
                _engineStatus.value = EngineStatus.NoModel
            }
            ModelStore.delete(getApplication(), spec)
            refreshModels()
        }
    }

    fun engineStatusNow(): EngineStatus = _engineStatus.value

    private fun updateModel(id: String, block: (ModelUiState) -> ModelUiState) {
        _models.update { list -> list.map { if (it.spec.id == id) block(it) else it } }
    }

    private fun addSystemNote(text: String) {
        _messages.update { it + ChatMessage(nextId++, text, fromUser = false) }
    }

    // ================= Practice =================

    fun startPractice(phrase: Phrase? = null) {
        val chosen = phrase ?: Curriculum.allPhrases.random()
        _practice.value = PracticeState(phrase = chosen)
    }

    fun updateAttempt(text: String) {
        _practice.update { it.copy(attempt = text) }
    }

    fun submitAttempt() {
        val state = _practice.value
        val phrase = state.phrase ?: return
        val score = Similarity.score(phrase.korean, state.attempt)
        val streak = prefs.recordPractice((score * 100).toInt())
        _practice.update {
            it.copy(
                score = score,
                feedback = Similarity.verdict(score),
                listening = false
            )
        }
        refreshStats(streak)
    }

    fun revealAnswer() {
        _practice.update { it.copy(attempt = it.phrase?.korean.orEmpty()) }
    }

    fun setListening(listening: Boolean) {
        _practice.update { it.copy(listening = listening) }
    }

    fun refreshStats(streak: Int = prefs.streakDays) {
        _stats.value = Stats(
            practiceCount = prefs.practiceCount,
            bestScore = prefs.bestScore,
            streak = streak,
            learnedLetters = prefs.learnedLetters.size,
            totalLetters = Curriculum.consonants.size + Curriculum.vowels.size
        )
    }

    // ================= Alphabet =================

    fun selectLetter(letter: String?) {
        _selectedLetter.value = if (_selectedLetter.value == letter) null else letter
    }

    fun toggleLearned(letter: String) {
        prefs.toggleLetter(letter)
        refreshStats()
    }

    fun isLetterLearned(letter: String): Boolean = prefs.learnedLetters.contains(letter)

    // ================= Explain =================

    /** Builds a jamo breakdown for [korean] without needing a model. */
    fun breakdown(korean: String): String {
        val syllables = korean.filter { Hangul.isSyllable(it) }
        if (syllables.isEmpty()) return ""
        val blocks = syllables.mapNotNull { c ->
            Hangul.decompose(c)?.let { "${c} = ${it.initial} + ${it.medial}" + (it.finalJamo?.let { f -> " + $f" } ?: "") }
        }
        return buildString {
            append(blocks.joinToString("\n"))
            append("\n\nRomanisation: ")
            append(Hangul.romanize(syllables.toString()))
            append("\nSyllables: ")
            append(syllables.length)
        }
    }

    /** Sends a lesson phrase to the tutor for a deeper explanation. */
    fun explainPhrase(phrase: Phrase) {
        sendMessage(
            "Explain this Korean phrase to me: ${phrase.korean} " +
                "(${phrase.english}). Break down the grammar and give one similar example."
        )
    }

    fun lessonOf(id: String): Lesson? = Curriculum.lesson(id)

    override fun onCleared() {
        super.onCleared()
        engine.release()
    }

    private fun readTotalRam(): Long {
        return try {
            val info = android.app.ActivityManager.MemoryInfo()
            val am = getApplication<Application>()
                .getSystemService(android.content.Context.ACTIVITY_SERVICE)
                    as android.app.ActivityManager
            am.getMemoryInfo(info)
            // totalMem is the physical RAM on API 16+.
            info.totalMem
        } catch (t: Throwable) {
            4L * 1024L * 1024L * 1024L
        }
    }
}
