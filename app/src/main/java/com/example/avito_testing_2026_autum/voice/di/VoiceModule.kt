package com.example.avito_testing_2026_autum.voice.di

import com.example.avito_testing_2026_autum.voice.data.recognition.AndroidSpeechRecognizer
import com.example.avito_testing_2026_autum.voice.domain.recognition.SpeechRecognizerContract
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val voiceModule = module {
    single<SpeechRecognizerContract> {
        AndroidSpeechRecognizer(
            context = androidContext(),
            dispatchers = get()
        )
    }
}