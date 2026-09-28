package com.example.avito_testing_2026_autum.core.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.avito_testing_2026_autum.R

enum class NavigationItem(
    val route: Screen,
    @StringRes val titleResId: Int,
    val icon: ImageVector
) {
    NOTES(
        route = Screen.Notes,
        titleResId = R.string.navigation_item_notes,
        icon = Icons.AutoMirrored.Filled.Notes
    ),
    TASKS(
        route = Screen.Tasks,
        titleResId = R.string.navigation_item_tasks,
        icon = Icons.Default.CheckCircle
    ),
    SETTINGS(
        route = Screen.Settings,
        titleResId = R.string.navigation_item_settings,
        icon = Icons.Default.Settings
    )
}