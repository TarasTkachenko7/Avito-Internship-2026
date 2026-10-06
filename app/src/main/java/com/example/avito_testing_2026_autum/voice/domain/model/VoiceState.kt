package com.example.avito_testing_2026_autum.voice.domain.model

sealed interface VoiceState {
    data object Idle: VoiceState
    data class Recording(val durationMs: Long = 0L) : VoiceState
    data object Processing : VoiceState
    data class Success(val text: String) : VoiceState
    data class Error(val message: VoiceError) : VoiceState
}

sealed interface VoiceError {
    data object MicrophonePermissionDenied : VoiceError
    data object RecordingFailed : VoiceError
    data object NetworkError : VoiceError
    data object RecognitionFailed : VoiceError
    data class Unknown(val message: String?) : VoiceError
}
