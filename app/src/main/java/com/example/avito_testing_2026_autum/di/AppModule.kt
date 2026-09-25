package com.example.avito_testing_2026_autum.di

import com.example.avito_testing_2026_autum.ai.di.aiModule
import com.example.avito_testing_2026_autum.core.di.coreModule
import com.example.avito_testing_2026_autum.notes.di.notesModule
import com.example.avito_testing_2026_autum.settings.di.settingsModule
import com.example.avito_testing_2026_autum.tasks.di.tasksModule
import org.koin.dsl.module

val appModule = module {
    includes(
        coreModule,
        aiModule,
        notesModule,
        settingsModule,
        tasksModule
    )
}