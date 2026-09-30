package com.example.avito_testing_2026_autum.notes.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.avito_testing_2026_autum.notes.presentation.NotesScreenRoot
import com.example.avito_testing_2026_autum.notes.presentation.editor.NoteEditorScreenRoot
import com.example.avito_testing_2026_autum.root.navigation.Screen

fun NavGraphBuilder.notesGraph(
    onNavigateToEditor: (Long?) -> Unit,
    onBackClick: () -> Unit
) {
    composable<Screen.Notes> {
        NotesScreenRoot(
            onNavigateToEditor = onNavigateToEditor
        )
    }

    composable<Screen.NoteEditor> { backStackEntry ->
        val args = backStackEntry.toRoute<Screen.NoteEditor>()
        NoteEditorScreenRoot(
            noteId = args.noteId,
            onNavigateBack = onBackClick
        )
    }
}