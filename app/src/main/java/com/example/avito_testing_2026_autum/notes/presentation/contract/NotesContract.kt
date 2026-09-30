package com.example.avito_testing_2026_autum.notes.presentation.contract

import com.example.avito_testing_2026_autum.notes.domain.model.Note

enum class SortType {
    DATE_DESC,
    DATE_ASC
}

data class NotesUiState (
    val isLoading: Boolean = true,
    val notes: List<Note> = emptyList(),
    val searchQuery: String = "",
    val isDeleteModeActive: Boolean = false,
    val sortType: SortType = SortType.DATE_DESC,
    val noteIdToDelete: Long? = null
)

sealed interface NotesEvent {
    data object OnCreateNoteClicked : NotesEvent
    data object OnToggleDeleteMode : NotesEvent
    data class OnNoteClicked(val noteId: Long) : NotesEvent
    data class OnDeleteNote(val noteId: Long) : NotesEvent
    data object OnConfirmDelete : NotesEvent
    data object OnDismissDeleteDialog : NotesEvent
    data class OnSearchQueryChanged(val query: String) : NotesEvent
    data class OnSortClicked(val sortType: SortType) : NotesEvent
}

sealed interface NotesEffect {
    data class NavigateToEditor(val noteId: Long) : NotesEffect
}