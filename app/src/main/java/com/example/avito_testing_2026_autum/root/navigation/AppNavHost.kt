package com.example.avito_testing_2026_autum.root.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.avito_testing_2026_autum.notes.presentation.NotesScreenRoot
import com.example.avito_testing_2026_autum.notes.presentation.editor.NoteEditorScreenRoot
import com.example.avito_testing_2026_autum.settings.presentation.SettingsScreenRoot
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
            SettingsScreenRoot()
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