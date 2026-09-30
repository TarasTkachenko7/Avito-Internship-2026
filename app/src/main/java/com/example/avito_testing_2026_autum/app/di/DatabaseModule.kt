package com.example.avito_testing_2026_autum.app.di

import androidx.room.Room
import com.example.avito_testing_2026_autum.app.local.database.AppDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {

    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "avito_database"
        ).build()
    }

    single { get<AppDatabase>().noteDao() }
    single { get<AppDatabase>().taskDao() }

}