package com.example.avito_testing_2026_autum.notes.presentation

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.avito_testing_2026_autum.R
import com.example.avito_testing_2026_autum.notes.presentation.components.DeleteConfirmationDialog
import com.example.avito_testing_2026_autum.notes.presentation.components.NoteItem
import com.example.avito_testing_2026_autum.notes.presentation.components.NotesActionRow
import com.example.avito_testing_2026_autum.notes.presentation.components.NotesSearchBar
import com.example.avito_testing_2026_autum.notes.presentation.contract.NotesEffect
import com.example.avito_testing_2026_autum.notes.presentation.contract.NotesEvent
import com.example.avito_testing_2026_autum.notes.presentation.contract.NotesUiState
import com.example.avito_testing_2026_autum.notes.presentation.models.NoteUiModel
import kotlinx.collections.immutable.ImmutableList
import org.koin.androidx.compose.koinViewModel

@Composable
fun NotesScreenRoot(
    viewModel: NotesViewModel = koinViewModel(),
    onNavigateToEditor: (Long?) -> Unit,
    modifier: Modifier = Modifier
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
        onEvent = viewModel::handleEvent,
        modifier = modifier
    )
}

@Composable
private fun NotesScreenContent(
    state: NotesUiState,
    onEvent: (NotesEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    var searchQueryInput by rememberSaveable { mutableStateOf(state.appliedSearchQuery) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            },
        floatingActionButton = {
            if (!state.isDeleteModeActive) {
                FloatingActionButton(
                    onClick = { onEvent(NotesEvent.Action.CreateNoteClicked) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.create_note)
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        ) {
            Text(
                text = stringResource(R.string.notes_title),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
            )

            NotesSearchBar(
                query = searchQueryInput,
                isDeleteModeActive = state.isDeleteModeActive,
                onQueryChange = { searchQueryInput = it },
                onSearchTriggered = { onEvent(NotesEvent.Query.SearchSubmit(searchQueryInput)) },
                onClearQuery = {
                    searchQueryInput = ""
                    onEvent(NotesEvent.Query.SearchClear)
                },
                onToggleDeleteMode = { onEvent(NotesEvent.Action.ToggleDeleteMode) }
            )

            NotesActionRow(
                selectedSort = state.sortOrder,
                onSortSelected = { onEvent(NotesEvent.Query.SortSelected(it)) }
            )

            Spacer(modifier = Modifier.height(4.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when {
                    state.isLoading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }

                    state.notes.isEmpty() -> {
                        EmptyNotesState(
                            isSearchActive = state.appliedSearchQuery.isNotEmpty(),
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
    }

    if (state.noteIdToDelete != null) {
        DeleteConfirmationDialog(
            onConfirm = { onEvent(NotesEvent.Modal.ConfirmDelete) },
            onDismiss = { onEvent(NotesEvent.Modal.DismissDeleteDialog) }
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
        contentPadding = PaddingValues(top = 4.dp, bottom = 100.dp)
    ) {
        items(
            items = notes,
            key = { note -> note.id }
        ) { note ->
            NoteItem(
                modifier = Modifier.animateItem(),
                note = note,
                isDeleteModeActive = isDeleteModeActive,
                onNoteClick = { onEvent(NotesEvent.Action.NoteCardClicked(note.id)) },
                onDeleteClick = { onEvent(NotesEvent.Action.DeleteIconClicked(note.id)) }
            )
        }
    }
}

@Composable
private fun EmptyNotesState(
    isSearchActive: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.padding(horizontal = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (isSearchActive) stringResource(R.string.not_find) else stringResource(R.string.dont_have_notes),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}