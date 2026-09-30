package com.example.avito_testing_2026_autum.settings.di

import com.example.avito_testing_2026_autum.settings.data.repository.SettingsRepositoryImpl
import com.example.avito_testing_2026_autum.settings.domain.repository.SettingsRepository
import com.example.avito_testing_2026_autum.settings.presentation.SettingsViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val settingsModule = module {

    singleOf(::SettingsRepositoryImpl).bind<SettingsRepository>()

    viewModelOf(::SettingsViewModel)

}