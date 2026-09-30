package com.example.avito_testing_2026_autum.root.di

import androidx.room.Room
import com.example.avito_testing_2026_autum.root.local.database.AppDatabase
import com.example.avito_testing_2026_autum.root.local.datastore.provideSettingsDataStore
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val rootModule = module {

    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "avito_database"
        ).build()
    }

    single { provideSettingsDataStore(androidContext()) }

    single { get<AppDatabase>().noteDao() }
    single { get<AppDatabase>().taskDao() }

}