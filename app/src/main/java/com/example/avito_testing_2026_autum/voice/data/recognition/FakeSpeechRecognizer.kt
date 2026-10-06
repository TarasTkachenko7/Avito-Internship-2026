package com.example.avito_testing_2026_autum.voice.data.recognition

import com.example.avito_testing_2026_autum.voice.domain.model.VoiceError
import com.example.avito_testing_2026_autum.voice.domain.model.VoiceState
import com.example.avito_testing_2026_autum.voice.domain.recognition.SpeechRecognizerContract
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FakeSpeechRecognizer(
    private val simulatedDelayMs: Long = 1000L,
    private val stubText: String = "Купить молоко и хлеб к вечеру",
    private val shouldFail: Boolean = false,
    private val failureError: VoiceError = VoiceError.RecognitionFailed
) : SpeechRecognizerContract {

    private val scope = CoroutineScope(Dispatchers.Default)
    private var simulationJob: Job? = null

    private val _voiceState = MutableStateFlow<VoiceState>(VoiceState.Idle)
    override val voiceState: StateFlow<VoiceState> = _voiceState.asStateFlow()

    override fun startListening() {
        simulationJob?.cancel()
        simulationJob = scope.launch {
            _voiceState.value = VoiceState.Recording()
            delay(simulatedDelayMs)
            _voiceState.value = VoiceState.Processing
            delay(simulatedDelayMs)

            if (shouldFail) {
                _voiceState.value = VoiceState.Error(failureError)
            } else {
                _voiceState.value = VoiceState.Success(stubText)
            }
        }
    }

    override fun stopListening() {
        // При ручной остановке сразу переводим в Processing, если шла запись
        if (_voiceState.value is VoiceState.Recording) {
            simulationJob?.cancel()
            simulationJob = scope.launch {
                _voiceState.value = VoiceState.Processing
                delay(simulatedDelayMs)
                if (shouldFail) {
                    _voiceState.value = VoiceState.Error(failureError)
                } else {
                    _voiceState.value = VoiceState.Success(stubText)
                }
            }
        }
    }

    override fun cancel() {
        simulationJob?.cancel()
        _voiceState.value = VoiceState.Idle
    }

    override fun reset() {
        simulationJob?.cancel()
        _voiceState.value = VoiceState.Idle
    }
}