package com.example.avito_testing_2026_autum.notes.presentation.contract.editor

import com.example.avito_testing_2026_autum.core.presentation.UiEffect
import com.example.avito_testing_2026_autum.core.presentation.UiEvent
import com.example.avito_testing_2026_autum.core.presentation.UiState

data class NoteEditorUiState(
    val isLoading: Boolean = true,
    val title: String = "",
    val text: String = "",
    val imageUri: String? = null
) : UiState {
    val isSaveButtonEnabled: Boolean
        get() = title.isNotBlank()
}

sealed interface NoteEditorEvent : UiEvent {
    data class OnTitleChanged(val title: String) : NoteEditorEvent
    data class OnTextChanged(val text: String) : NoteEditorEvent
    data class OnImageSelected(val imageUri: String?) : NoteEditorEvent
    data object OnSaveClicked : NoteEditorEvent
    data object OnBackClicked : NoteEditorEvent
}

sealed interface NoteEditorEffect : UiEffect {
    data object NavigateBack : NoteEditorEffect
}