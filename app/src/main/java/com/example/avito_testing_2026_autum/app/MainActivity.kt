package com.example.avito_testing_2026_autum.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.avito_testing_2026_autum.app.presentation.MainScreen
import com.example.avito_testing_2026_autum.ui.theme.AvitoNotesTasksTheme
import com.example.avito_testing_2026_autum.settings.presentation.SettingsThemeViewModel
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsViewModel: SettingsThemeViewModel = koinViewModel()
            val settings by settingsViewModel.settings.collectAsStateWithLifecycle()
            AvitoNotesTasksTheme(
                themeMode = settings.themeMode,
                accentColor = settings.accentColor
            ) {
                MainScreen()
            }
        }
    }
}
