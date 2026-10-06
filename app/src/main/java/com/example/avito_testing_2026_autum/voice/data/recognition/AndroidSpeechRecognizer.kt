package com.example.avito_testing_2026_autum.voice.data.recognition

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.example.avito_testing_2026_autum.core.dispatchers.DispatchersProvider
import com.example.avito_testing_2026_autum.voice.domain.model.VoiceError
import com.example.avito_testing_2026_autum.voice.domain.model.VoiceState
import com.example.avito_testing_2026_autum.voice.domain.recognition.SpeechRecognizerContract
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

class AndroidSpeechRecognizer(
    private val context: Context,
    private val dispatchers: DispatchersProvider
) : SpeechRecognizerContract {

    private val scope = CoroutineScope(SupervisorJob() + dispatchers.main)

    private val _voiceState = MutableStateFlow<VoiceState>(VoiceState.Idle)
    override val voiceState: StateFlow<VoiceState> = _voiceState.asStateFlow()

    private var speechRecognizer: SpeechRecognizer? = null

    private val recognizerIntent: Intent by lazy {
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale("ru").toLanguageTag())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
        }
    }

    private val recognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            _voiceState.value = VoiceState.Recording()
        }

        override fun onBeginningOfSpeech() {
            _voiceState.value = VoiceState.Recording()
        }

        override fun onRmsChanged(rmsdB: Float) = Unit

        override fun onBufferReceived(buffer: ByteArray?) = Unit

        override fun onEndOfSpeech() {
            _voiceState.value = VoiceState.Processing
        }

        override fun onError(errorCode: Int) {
            val error = mapErrorCodeToVoiceError(errorCode)
            _voiceState.value = VoiceState.Error(error)
        }

        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val recognizedText = matches?.firstOrNull()?.trim().orEmpty()

            if (recognizedText.isNotBlank()) {
                _voiceState.value = VoiceState.Success(recognizedText)
            } else {
                _voiceState.value = VoiceState.Error(VoiceError.RecognitionFailed)
            }
        }

        override fun onPartialResults(partialResults: Bundle?) = Unit

        override fun onEvent(eventType: Int, params: Bundle?) = Unit
    }

    override fun startListening() {
        scope.launch {
            if (!SpeechRecognizer.isRecognitionAvailable(context)) {
                _voiceState.value = VoiceState.Error(
                    VoiceError.Unknown("Сервис распознавания недоступен на устройстве")
                )
                return@launch
            }

            cleanUpRecognizer()

            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(recognitionListener)
                startListening(recognizerIntent)
            }
        }
    }

    override fun stopListening() {
        scope.launch {
            speechRecognizer?.stopListening()
            _voiceState.value = VoiceState.Processing
        }
    }

    override fun cancel() {
        scope.launch {
            cleanUpRecognizer()
            _voiceState.value = VoiceState.Idle
        }
    }

    override fun reset() {
        scope.launch {
            _voiceState.value = VoiceState.Idle
        }
    }

    private fun cleanUpRecognizer() {
        speechRecognizer?.destroy()
        speechRecognizer = null
    }

    private fun mapErrorCodeToVoiceError(code: Int): VoiceError {
        return when (code) {
            SpeechRecognizer.ERROR_AUDIO -> VoiceError.RecordingFailed
            SpeechRecognizer.ERROR_NETWORK,
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT,
            SpeechRecognizer.ERROR_SERVER -> VoiceError.NetworkError
            SpeechRecognizer.ERROR_NO_MATCH,
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> VoiceError.RecognitionFailed
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> VoiceError.MicrophonePermissionDenied
            else -> VoiceError.Unknown("Код ошибки распознавания: $code")
        }
    }
}