package com.example.avito_testing_2026_autum.core.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.avito_testing_2026_autum.notes.presentation.NotesScreenRoot
import com.example.avito_testing_2026_autum.notes.presentation.editor.NoteEditorScreenRoot
import com.example.avito_testing_2026_autum.tasks.presentation.TasksScreenRoot

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Notes,
        modifier = modifier
    ) {
        composable<Screen.Notes> {
            NotesScreenRoot(
                onNavigateToEditor = { noteId ->
                    navController.navigate(Screen.NoteEditor(noteId))
                }
            )
        }

        composable<Screen.Tasks> {
            TasksScreenRoot()
        }

        composable<Screen.Settings> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Здесь будут Настройки")
            }
        }

        composable<Screen.NoteEditor> { backStackEntry ->
            val args = backStackEntry.toRoute<Screen.NoteEditor>()

            NoteEditorScreenRoot(
                noteId = args.noteId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}