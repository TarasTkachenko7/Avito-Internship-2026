package com.example.avito_testing_2026_autum.core.di

import androidx.room.Room
import com.example.avito_testing_2026_autum.core.data.local.AppDatabase
import com.example.avito_testing_2026_autum.core.dispatchers.DispatchersProvider
import com.example.avito_testing_2026_autum.core.dispatchers.StandardDispatcher
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val coreModule = module {

    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "avito_database"
        ).build()
    }

    single { get<AppDatabase>().noteDao }
    single { get<AppDatabase>().taskDao }

    singleOf(::StandardDispatcher).bind<DispatchersProvider>()

}