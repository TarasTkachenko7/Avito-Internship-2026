package com.example.avito_testing_2026_autum.voice.domain.recognition

import com.example.avito_testing_2026_autum.voice.domain.model.VoiceState
import kotlinx.coroutines.flow.StateFlow

interface SpeechRecognizerContract {
    val voiceState: StateFlow<VoiceState>
    fun startListening()
    fun stopListening()
    fun cancel()
    fun reset()
}