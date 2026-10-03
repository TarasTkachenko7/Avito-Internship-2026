package com.example.avito_testing_2026_autum.notes.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.avito_testing_2026_autum.notes.domain.usecases.DeleteNoteUseCase
import com.example.avito_testing_2026_autum.notes.domain.usecases.GetNotesUseCase
import com.example.avito_testing_2026_autum.notes.presentation.contract.NotesEffect
import com.example.avito_testing_2026_autum.notes.presentation.contract.NotesEvent
import com.example.avito_testing_2026_autum.notes.presentation.contract.NotesUiState
import com.example.avito_testing_2026_autum.notes.presentation.mapper.toUiModel
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class NotesViewModel(
    private val getNotesUseCase: GetNotesUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(NotesUiState())
    val state: StateFlow<NotesUiState> = _state.asStateFlow()

    private val _effect = Channel<NotesEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        observeNotes()
    }

    private fun observeNotes() {
        viewModelScope.launch {
            state
                .map { it.searchQuery to it.sortOrder }
                .distinctUntilChanged()
                .flatMapLatest { (query, sortOrder) ->
                    getNotesUseCase(query, sortOrder)
                }
                .map { domainNotes ->
                    domainNotes.map { it.toUiModel() }.toPersistentList()
                }
                .catch { exception ->
                    Log.e("NotesViewModel", "Failed to observe notes", exception)
                    _state.update { it.copy(isLoading = false) }
                }
                .collect { notesList ->
                    _state.update { it.copy(notes = notesList, isLoading = false) }
                }
        }
    }

    fun handleEvent(event: NotesEvent) {
        when (event) {
            is NotesEvent.OnSearchQueryChanged -> {
                _state.update { it.copy(searchQuery = event.query) }
            }

            is NotesEvent.OnSortClicked -> {
                _state.update { it.copy(sortOrder = event.sortOrder) }
            }

            is NotesEvent.OnNoteClicked -> {
                if (!state.value.isDeleteModeActive) {
                    sendEffect(NotesEffect.NavigateToEditor(event.noteId))
                }
            }

            is NotesEvent.OnCreateNoteClicked -> {
                sendEffect(NotesEffect.NavigateToEditor(null))
            }

            is NotesEvent.OnToggleDeleteMode -> {
                _state.update {
                    it.copy(
                        isDeleteModeActive = !it.isDeleteModeActive,
                        noteIdToDelete = null
                    )
                }
            }

            is NotesEvent.OnDeleteNote -> {
                _state.update { it.copy(noteIdToDelete = event.noteId) }
            }

            is NotesEvent.OnConfirmDelete -> {
                val noteId = state.value.noteIdToDelete
                if (noteId != null) {
                    viewModelScope.launch {
                        deleteNoteUseCase(noteId)
                        _state.update { it.copy(noteIdToDelete = null) }
                    }
                }
            }

            is NotesEvent.OnDismissDeleteDialog -> {
                _state.update { it.copy(noteIdToDelete = null) }
            }
        }
    }

    private fun sendEffect(effect: NotesEffect) {
        _effect.trySend(effect)
    }

}