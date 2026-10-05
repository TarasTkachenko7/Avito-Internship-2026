package com.example.avito_testing_2026_autum.notes.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.avito_testing_2026_autum.R
import com.example.avito_testing_2026_autum.notes.presentation.components.DeleteConfirmationDialog
import com.example.avito_testing_2026_autum.notes.presentation.components.NoteItem
import com.example.avito_testing_2026_autum.notes.presentation.components.NotesTopBar
import com.example.avito_testing_2026_autum.notes.presentation.contract.NotesEffect
import com.example.avito_testing_2026_autum.notes.presentation.contract.NotesEvent
import com.example.avito_testing_2026_autum.notes.presentation.contract.NotesUiState
import com.example.avito_testing_2026_autum.notes.presentation.models.NoteUiModel
import kotlinx.collections.immutable.ImmutableList
import org.koin.androidx.compose.koinViewModel

@Composable
fun NotesScreenRoot(
    viewModel: NotesViewModel = koinViewModel(),
    onNavigateToEditor: (Long?) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is NotesEffect.NavigateToEditor -> onNavigateToEditor(effect.noteId)
            }
        }
    }

    NotesScreenContent(
        state = state,
        onEvent = viewModel::handleEvent
    )
}

@Composable
private fun NotesScreenContent(
    state: NotesUiState,
    onEvent: (NotesEvent) -> Unit
) {
    Scaffold(
        topBar = {
            NotesTopBar(
                searchQuery = state.searchQuery,
                isDeleteModeActive = state.isDeleteModeActive,
                onSearchQueryChange = { onEvent(NotesEvent.OnSearchQueryChanged(it)) },
                onAppliedSearchQueryChanged = { onEvent(NotesEvent.OnSearchClicked) },
                onSortSelect = { onEvent(NotesEvent.OnSortClicked(it)) },
                onToggleDeleteMode = { onEvent(NotesEvent.OnToggleDeleteMode) }
            )
        },
        floatingActionButton = {
            if (!state.isDeleteModeActive) {
                FloatingActionButton(onClick = { onEvent(NotesEvent.OnCreateNoteClicked) }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.create_note)
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                state.notes.isEmpty() -> {
                    EmptyNotesState(
                        searchQuery = state.searchQuery,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    NotesList(
                        notes = state.notes,
                        isDeleteModeActive = state.isDeleteModeActive,
                        onEvent = onEvent
                    )
                }
            }
        }
    }

    if (state.noteIdToDelete != null) {
        DeleteConfirmationDialog(
            onConfirm = { onEvent(NotesEvent.OnConfirmDelete) },
            onDismiss = { onEvent(NotesEvent.OnDismissDeleteDialog) }
        )
    }
}

@Composable
private fun NotesList(
    notes: ImmutableList<NoteUiModel>,
    isDeleteModeActive: Boolean,
    onEvent: (NotesEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(
            items = notes,
            key = { note -> note.id }
        ) { note ->
            NoteItem(
                note = note,
                isDeleteModeActive = isDeleteModeActive,
                onNoteClick = { onEvent(NotesEvent.OnNoteClicked(note.id)) },
                onDeleteClick = { onEvent(NotesEvent.OnDeleteNote(note.id)) }
            )
        }
    }
}

@Composable
private fun EmptyNotesState(
    searchQuery: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = if (searchQuery.isEmpty()) {
            stringResource(R.string.dont_have_notes)
        } else {
            stringResource(R.string.not_find)
        },
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = modifier
    )
}