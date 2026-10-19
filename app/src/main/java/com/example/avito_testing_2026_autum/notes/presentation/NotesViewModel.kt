package com.example.avito_testing_2026_autum.notes.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.avito_testing_2026_autum.R
import com.example.avito_testing_2026_autum.core.utils.UiText
import com.example.avito_testing_2026_autum.notes.domain.usecases.DeleteNoteUseCase
import com.example.avito_testing_2026_autum.notes.domain.usecases.GetNotesUseCase
import com.example.avito_testing_2026_autum.notes.presentation.contract.NotesEffect
import com.example.avito_testing_2026_autum.notes.presentation.contract.NotesEvent
import com.example.avito_testing_2026_autum.notes.presentation.contract.NotesUiState
import com.example.avito_testing_2026_autum.notes.presentation.mapper.toUiModel
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Dispatchers
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
import kotlinx.coroutines.withContext

@OptIn(ExperimentalCoroutinesApi::class)
class NotesViewModel(
    private val getNotesUseCase: GetNotesUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(NotesUiState())
    val state: StateFlow<NotesUiState> = _state.asStateFlow()

    private val _effect = Channel<NotesEffect>(capacity = Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        observeNotes()
    }

    private fun observeNotes() {
        viewModelScope.launch {
            _state.map { Triple(it.appliedSearchQuery, it.sortOrder, it.reloadVersion) }
                .distinctUntilChanged()
                .flatMapLatest { (query, sortOrder, _) ->
                    getNotesUseCase(query, sortOrder)
                        .map { domainNotes ->
                            withContext(Dispatchers.Default) {
                                domainNotes.map { it.toUiModel() }.toPersistentList()
                            }
                        }
                        .catch {
                            _state.update { it.copy(hasLoadError = true, isLoading = false) }
                        }
                }.collect { notesList ->
                    _state.update { it.copy(notes = notesList, isLoading = false, hasLoadError = false) }
                }
        }
    }

    fun handleEvent(event: NotesEvent) {
        when (event) {
            is NotesEvent.Query -> handleQuery(event)
            is NotesEvent.Action -> handleAction(event)
            is NotesEvent.Modal -> handleModal(event)
        }
    }

    private fun handleQuery(event: NotesEvent.Query) {
        when (event) {
            is NotesEvent.Query.SearchSubmit -> {
                _state.update {
                    if (it.appliedSearchQuery == event.query) it else {
                        it.copy(appliedSearchQuery = event.query, isLoading = true, hasLoadError = false)
                    }
                }
            }

            is NotesEvent.Query.SearchClear -> {
                _state.update {
                    if (it.appliedSearchQuery.isEmpty()) it else {
                        it.copy(appliedSearchQuery = "", isLoading = true, hasLoadError = false)
                    }
                }
            }

            is NotesEvent.Query.SortSelected -> {
                _state.update {
                    if (it.sortOrder == event.sortOrder) it else {
                        it.copy(sortOrder = event.sortOrder, isLoading = true, hasLoadError = false)
                    }
                }
            }
        }
    }

    private fun handleAction(event: NotesEvent.Action) {
        when (event) {
            is NotesEvent.Action.NoteCardClicked -> {
                if (!state.value.isDeleteModeActive) {
                    sendEffect(NotesEffect.NavigateToEditor(event.id))
                }
            }

            is NotesEvent.Action.CreateNoteClicked -> {
                sendEffect(NotesEffect.NavigateToEditor(null))
            }

            is NotesEvent.Action.ToggleDeleteMode -> {
                _state.update {
                    it.copy(
                        isDeleteModeActive = !it.isDeleteModeActive,
                        noteIdToDelete = null
                    )
                }
            }

            is NotesEvent.Action.DeleteIconClicked -> {
                _state.update { it.copy(noteIdToDelete = event.id) }
            }

            is NotesEvent.Action.RetryLoading -> {
                _state.update {
                    it.copy(
                        hasLoadError = false,
                        isLoading = true,
                        reloadVersion = it.reloadVersion + 1
                    )
                }
            }
        }
    }

    private fun handleModal(event: NotesEvent.Modal) {
        when (event) {
            is NotesEvent.Modal.ConfirmDelete -> {
                val noteId = state.value.noteIdToDelete
                if (noteId != null) {
                    viewModelScope.launch {
                        deleteNoteUseCase(noteId).onSuccess {
                            _state.update { it.copy(noteIdToDelete = null) }
                        }.onFailure {
                            sendEffect(NotesEffect.ShowMessage(UiText.StringResource(R.string.notes_error_delete)))
                        }
                    }
                }
            }

            is NotesEvent.Modal.DismissDeleteDialog -> {
                _state.update { it.copy(noteIdToDelete = null) }
            }
        }
    }

    private fun sendEffect(effect: NotesEffect) {
        _effect.trySend(effect)
    }

}
