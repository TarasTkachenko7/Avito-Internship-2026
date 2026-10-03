package com.example.avito_testing_2026_autum.notes.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.avito_testing_2026_autum.app.navigation.Screen
import com.example.avito_testing_2026_autum.notes.presentation.NotesScreenRoot
import com.example.avito_testing_2026_autum.notes.presentation.editor.NoteEditorScreenRoot

fun NavGraphBuilder.notesGraph(
    onNavigateToEditor: (Long?) -> Unit,
    onBackClick: () -> Unit
) {
    composable<Screen.Notes> {
        NotesScreenRoot(
            onNavigateToEditor = onNavigateToEditor
        )
    }

    composable<Screen.NoteEditor> {
        NoteEditorScreenRoot(
            onNavigateBack = onBackClick
        )
    }
}