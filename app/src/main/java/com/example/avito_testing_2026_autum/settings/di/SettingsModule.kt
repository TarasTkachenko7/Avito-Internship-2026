package com.example.avito_testing_2026_autum.settings.di

import com.example.avito_testing_2026_autum.settings.data.local.provideSettingsDataStore
import com.example.avito_testing_2026_autum.settings.data.repository.SettingsRepositoryImpl
import com.example.avito_testing_2026_autum.settings.domain.repository.SettingsRepository
import com.example.avito_testing_2026_autum.settings.domain.usecases.GetAccentColorUseCase
import com.example.avito_testing_2026_autum.settings.domain.usecases.GetThemeModeUseCase
import com.example.avito_testing_2026_autum.settings.domain.usecases.SetAccentColorUseCase
import com.example.avito_testing_2026_autum.settings.domain.usecases.SetThemeModeUseCase
import com.example.avito_testing_2026_autum.settings.presentation.SettingsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val settingsModule = module {

    single { provideSettingsDataStore(androidContext()) }
    singleOf(::SettingsRepositoryImpl).bind<SettingsRepository>()

    factoryOf(::GetThemeModeUseCase)
    factoryOf(::GetAccentColorUseCase)
    factoryOf(::SetThemeModeUseCase)
    factoryOf(::SetAccentColorUseCase)

    viewModelOf(::SettingsViewModel)

}