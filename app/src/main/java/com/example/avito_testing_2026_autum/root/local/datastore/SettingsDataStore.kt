package com.example.avito_testing_2026_autum.root.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

private const val SETTINGS_PREFERENCES_NAME = "settings_preferences"

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = SETTINGS_PREFERENCES_NAME
)

fun provideSettingsDataStore(context: Context): DataStore<Preferences> {
    return context.applicationContext.settingsDataStore
}