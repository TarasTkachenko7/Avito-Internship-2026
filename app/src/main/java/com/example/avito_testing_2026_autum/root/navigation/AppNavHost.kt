package com.example.avito_testing_2026_autum.root.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.example.avito_testing_2026_autum.notes.presentation.navigation.notesGraph
import com.example.avito_testing_2026_autum.settings.presentation.navigation.settingsGraph
import com.example.avito_testing_2026_autum.tasks.presentation.navigation.tasksGraph

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Notes,
        modifier = modifier
    ) {
        notesGraph(
            onNavigateToEditor = { noteId ->
                navController.navigate(Screen.NoteEditor(noteId))
            },
            onBackClick = {
                navController.popBackStack()
            }
        )

        tasksGraph()

        settingsGraph()
    }
}