package com.example.avito_testing_2026_autum.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.avito_testing_2026_autum.settings.domain.model.AccentColor
import com.example.avito_testing_2026_autum.settings.domain.model.ThemeMode

@Composable
fun AvitoNotesTasksTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    accentColor: AccentColor = AccentColor.DEFAULT,
    content: @Composable () -> Unit
) {
    val isDarkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val colorScheme = createColorScheme(isDarkTheme, accentColor)
    val view = LocalView.current
    val activity = LocalContext.current.findActivity()

    SideEffect {
        activity?.window?.let { window ->
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !isDarkTheme
                isAppearanceLightNavigationBars = !isDarkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

private fun createColorScheme(isDark: Boolean, accent: AccentColor): ColorScheme {
    val primary = when (accent) {
        AccentColor.DEFAULT -> AccentBlue
        AccentColor.PURPLE -> AccentViolet
        AccentColor.GREEN -> AccentGreen
        AccentColor.ORANGE -> AccentOrange
        AccentColor.PINK -> AccentPurple
    }
    val onPrimary = if (accent == AccentColor.DEFAULT) Color(0xFF0F1216) else Color.White

    return if (isDark) {
        darkColorScheme(
            primary = primary,
            onPrimary = onPrimary,
            secondary = primary,
            onSecondary = onPrimary,
            tertiary = primary,
            onTertiary = onPrimary,
            background = Color(0xFF0D1013),
            onBackground = Color(0xFFF4F5F7),
            surface = Color(0xFF181B1F),
            onSurface = Color(0xFFF4F5F7),
            surfaceVariant = Color(0xFF22262B),
            onSurfaceVariant = Color(0xFF9BA0A6),
            outline = Color(0xFF767B81),
            outlineVariant = Color(0xFF41464D)
        )
    } else {
        lightColorScheme(
            primary = primary,
            onPrimary = onPrimary,
            secondary = primary,
            onSecondary = onPrimary,
            tertiary = primary,
            onTertiary = onPrimary,
            background = Color(0xFFF3F4F6),
            onBackground = Color(0xFF0F1216),
            surface = Color.White,
            onSurface = Color(0xFF0F1216),
            surfaceVariant = Color(0xFFE7E9ED),
            onSurfaceVariant = Color(0xFF626A75),
            outline = Color(0xFF6D7480),
            outlineVariant = Color(0xFFD0D4DA)
        )
    }
}
