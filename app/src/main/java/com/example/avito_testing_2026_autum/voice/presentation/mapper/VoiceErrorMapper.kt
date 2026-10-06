// voice/presentation/mapper/VoiceErrorMapper.kt
package com.example.avito_testing_2026_autum.voice.presentation.mapper

import com.example.avito_testing_2026_autum.R
import com.example.avito_testing_2026_autum.core.utils.UiText
import com.example.avito_testing_2026_autum.voice.domain.model.VoiceError

fun VoiceError.toUiText(): UiText {
    return when (this) {
        VoiceError.MicrophonePermissionDenied -> UiText.StringResource(R.string.voice_error_permission_denied)
        VoiceError.RecordingFailed -> UiText.StringResource(R.string.voice_error_recording_failed)
        VoiceError.NetworkError -> UiText.StringResource(R.string.voice_error_network)
        VoiceError.RecognitionFailed -> UiText.StringResource(R.string.voice_error_no_speech)
        is VoiceError.Unknown -> message?.let { UiText.DynamicString(it) }
            ?: UiText.StringResource(R.string.voice_error_unknown)
    }
}