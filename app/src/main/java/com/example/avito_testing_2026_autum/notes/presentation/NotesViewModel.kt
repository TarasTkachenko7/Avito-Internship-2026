package com.example.avito_testing_2026_autum.notes.presentation

import androidx.lifecycle.viewModelScope
import com.example.avito_testing_2026_autum.core.presentation.BaseViewModel
import com.example.avito_testing_2026_autum.notes.domain.usecases.DeleteNoteUseCase
import com.example.avito_testing_2026_autum.notes.domain.usecases.GetNotesUseCase
import com.example.avito_testing_2026_autum.notes.presentation.contract.NotesEffect
import com.example.avito_testing_2026_autum.notes.presentation.contract.NotesEvent
import com.example.avito_testing_2026_autum.notes.presentation.contract.NotesUiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class NotesViewModel(
    private val getNotesUseCase: GetNotesUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase
): BaseViewModel<NotesUiState, NotesEvent, NotesEffect>(
    initialValue = NotesUiState()
) {

    init {
        observeNotes()
    }

    private fun observeNotes() {
        viewModelScope.launch {
            state
                .map { it.searchQuery to it.sortType }
                .distinctUntilChanged()
                .flatMapLatest { (query, sortType) ->
                    getNotesUseCase(query, sortType)
                }
                .collect { notesList ->
                    setState { it.copy(notes = notesList, isLoading = false) }
                }
        }
    }

    override fun handleEvent(event: NotesEvent) {
        when (event) {
            is NotesEvent.OnSearchQueryChanged -> {
                setState { it.copy(searchQuery = event.query) }
            }
            is NotesEvent.OnSortClicked -> {
                setState { it.copy(sortType = event.sortType) }
            }
            is NotesEvent.OnNoteClicked -> {
                if (!state.value.isDeleteModeActive) {
                    sendEffect { NotesEffect.NavigateToEditor(event.noteId) }
                }
            }
            is NotesEvent.OnCreateNoteClicked -> {
                sendEffect { NotesEffect.NavigateToEditor(-1L) }
            }
            is NotesEvent.OnToggleDeleteMode -> {
                setState {
                    it.copy(
                        isDeleteModeActive = !it.isDeleteModeActive,
                        noteIdToDelete = null
                    )
                }
            }
            is NotesEvent.OnDeleteNote -> {
                setState { it.copy(noteIdToDelete = event.noteId) }
            }
            is NotesEvent.OnConfirmDelete -> {
                val noteId = state.value.noteIdToDelete
                if (noteId != null) {
                    viewModelScope.launch {
                        deleteNoteUseCase(noteId)
                        setState { it.copy(noteIdToDelete = null) }
                    }
                }
            }
            is NotesEvent.OnDismissDeleteDialog -> {
                setState { it.copy(noteIdToDelete = null) }
            }
        }
    }

}