package com.example.avito_testing_2026_autum.app.di

import com.example.avito_testing_2026_autum.ai.di.aiNetworkModule
import com.example.avito_testing_2026_autum.core.di.coreModule
import com.example.avito_testing_2026_autum.notes.di.notesModule
import com.example.avito_testing_2026_autum.settings.di.settingsModule
import com.example.avito_testing_2026_autum.tasks.di.tasksModule
import com.example.avito_testing_2026_autum.voice.di.voiceModule
import org.koin.dsl.module

val appModule = module {
    includes(
        coreModule,
        databaseModule,
        aiNetworkModule,
        notesModule,
        settingsModule,
        tasksModule,
        voiceModule
    )
}