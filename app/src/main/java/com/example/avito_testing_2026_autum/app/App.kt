package com.example.avito_testing_2026_autum.app

import android.app.Application
import com.example.avito_testing_2026_autum.app.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@App)
            modules(appModule)
        }
    }
}