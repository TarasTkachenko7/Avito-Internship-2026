package com.example.avito_testing_2026_autum.notes.presentation.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.avito_testing_2026_autum.notes.domain.model.Note
import com.example.avito_testing_2026_autum.notes.domain.usecases.editor.GetNoteByIdUseCase
import com.example.avito_testing_2026_autum.notes.domain.usecases.editor.UpsertNoteUseCase
import com.example.avito_testing_2026_autum.notes.presentation.contract.editor.NoteEditorEffect
import com.example.avito_testing_2026_autum.notes.presentation.contract.editor.NoteEditorEvent
import com.example.avito_testing_2026_autum.notes.presentation.contract.editor.NoteEditorUiState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NoteEditorViewModel(
    private val noteId: Long?,
    private val getNoteByIdUseCase: GetNoteByIdUseCase,
    private val upsertNoteUseCase: UpsertNoteUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(NoteEditorUiState())
    val state: StateFlow<NoteEditorUiState> = _state.asStateFlow()

    private val _effect = Channel<NoteEditorEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private var originalCreatedAt: Long = 0L

    init {
        if (noteId != null) {
            loadNote(noteId)
        } else {
            _state.update { it.copy(isLoading = false) }
        }
    }

    private fun loadNote(id: Long) {
        viewModelScope.launch {
            val note = getNoteByIdUseCase(id)
            if (note != null) {
                originalCreatedAt = note.createdAt
                _state.update {
                    it.copy(
                        title = note.title,
                        text = note.text.orEmpty(),
                        imageUri = note.imageUri,
                        isLoading = false
                    )
                }
            } else {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun handleEvent(event: NoteEditorEvent) {
        when (event) {
            is NoteEditorEvent.OnTitleChanged -> _state.update { it.copy(title = event.title) }
            is NoteEditorEvent.OnTextChanged -> _state.update { it.copy(text = event.text) }
            is NoteEditorEvent.OnImageSelected -> {
                _state.update {
                    it.copy(
                        imageUri = event.imageUri,
                        showAttachmentDialog = false
                    )
                }
            }
            is NoteEditorEvent.OnAttachmentClicked -> _state.update { it.copy(showAttachmentDialog = true) }
            is NoteEditorEvent.OnDismissAttachmentDialog -> _state.update { it.copy(showAttachmentDialog = false) }
            is NoteEditorEvent.OnSaveClicked -> saveNote()
            is NoteEditorEvent.OnBackClicked -> sendEffect(NoteEditorEffect.NavigateBack)
        }
    }

    private fun saveNote() {
        val currentState = state.value
        if (!currentState.isSaveButtonEnabled) return

        viewModelScope.launch {
            val isNewNote = noteId == -1L
            val noteToSave = Note(
                id = if (isNewNote) 0L else noteId,
                title = currentState.title.trim(),
                text = currentState.text.trim().ifBlank { null },
                imageUri = currentState.imageUri,
                createdAt = if (isNewNote) System.currentTimeMillis() else originalCreatedAt
            )
            upsertNoteUseCase(noteToSave)
            sendEffect(NoteEditorEffect.NavigateBack)
        }
    }

    private fun sendEffect(effect: NoteEditorEffect) {
        viewModelScope.launch { _effect.send(effect) }
    }
}