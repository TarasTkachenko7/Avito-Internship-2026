package com.example.avito_testing_2026_autum.core.di

import com.example.avito_testing_2026_autum.core.dispatchers.DispatchersProvider
import com.example.avito_testing_2026_autum.core.dispatchers.StandardDispatcher
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val coreModule = module {
    singleOf(::StandardDispatcher).bind<DispatchersProvider>()
}