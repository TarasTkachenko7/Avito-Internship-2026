package com.example.avito_testing_2026_autum.root.di

import androidx.room.Room
import com.example.avito_testing_2026_autum.core.dispatchers.DispatchersProvider
import com.example.avito_testing_2026_autum.core.dispatchers.StandardDispatcher
import com.example.avito_testing_2026_autum.root.local.database.AppDatabase
import com.example.avito_testing_2026_autum.root.local.datastore.dataStore
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val rootModule = module {

    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "avito_database"
        ).build()
    }

    single { androidContext().dataStore }

    single { get<AppDatabase>().noteDao }
    single { get<AppDatabase>().taskDao }

    singleOf(::StandardDispatcher).bind<DispatchersProvider>()

}