package com.example.avito_testing_2026_autum.notes.presentation.editor

import androidx.lifecycle.viewModelScope
import com.example.avito_testing_2026_autum.core.presentation.BaseViewModel
import com.example.avito_testing_2026_autum.notes.domain.model.Note
import com.example.avito_testing_2026_autum.notes.domain.usecases.editor.GetNoteByIdUseCase
import com.example.avito_testing_2026_autum.notes.domain.usecases.editor.UpsertNoteUseCase
import com.example.avito_testing_2026_autum.notes.presentation.contract.editor.NoteEditorEffect
import com.example.avito_testing_2026_autum.notes.presentation.contract.editor.NoteEditorEvent
import com.example.avito_testing_2026_autum.notes.presentation.contract.editor.NoteEditorUiState
import kotlinx.coroutines.launch

class NoteEditorViewModel(
    private val noteId: Long,
    private val getNoteByIdUseCase: GetNoteByIdUseCase,
    private val upsertNoteUseCase: UpsertNoteUseCase
): BaseViewModel<NoteEditorUiState, NoteEditorEvent, NoteEditorEffect>(
    initialValue = NoteEditorUiState()
) {

    private var originalCreatedAt: Long = 0L

    init {
        if (noteId != -1L) {
            loadNote(noteId)
        } else {
            setState { it.copy(isLoading = false) }
        }
    }

    private fun loadNote(id: Long) {
        viewModelScope.launch {
            val note = getNoteByIdUseCase(id)
            if (note != null) {
                originalCreatedAt = note.createdAt
                setState {
                    it.copy(
                        title = note.title,
                        text = note.text.orEmpty(),
                        imageUri = note.imageUri,
                        isLoading = false
                    )
                }
            } else {
                setState { it.copy(isLoading = false) }
            }
        }
    }

    override fun handleEvent(event: NoteEditorEvent) {
        when (event) {
            is NoteEditorEvent.OnTitleChanged -> {
                setState { it.copy(title = event.title) }
            }
            is NoteEditorEvent.OnTextChanged -> {
                setState { it.copy(text = event.text) }
            }
            is NoteEditorEvent.OnImageSelected -> {
                setState { it.copy(imageUri = event.imageUri) }
            }
            is NoteEditorEvent.OnSaveClicked -> {
                saveNote()
            }
            is NoteEditorEvent.OnBackClicked -> {
                sendEffect { NoteEditorEffect.NavigateBack }
            }
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
            sendEffect { NoteEditorEffect.NavigateBack }
        }
    }

}