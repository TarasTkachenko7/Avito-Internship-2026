package com.example.avito_testing_2026_autum.settings.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.avito_testing_2026_autum.app.navigation.Screen
import com.example.avito_testing_2026_autum.settings.presentation.SettingsScreenRoot

fun NavGraphBuilder.settingsGraph() {
    composable<Screen.Settings> {
        SettingsScreenRoot()
    }
}