package com.example.avito_testing_2026_autum

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.avito_testing_2026_autum.root.presentation.MainScreen
import com.example.avito_testing_2026_autum.ui.theme.AvitoNotesTasksTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AvitoNotesTasksTheme {
                MainScreen()
            }
        }
    }
}