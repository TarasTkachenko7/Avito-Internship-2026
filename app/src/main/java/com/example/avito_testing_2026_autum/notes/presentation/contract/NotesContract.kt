package com.example.avito_testing_2026_autum.notes.presentation.contract

import com.example.avito_testing_2026_autum.notes.domain.model.NoteSortOrder
import com.example.avito_testing_2026_autum.notes.presentation.models.NoteUiModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class NotesUiState (
    val isLoading: Boolean = true,
    val notes: ImmutableList<NoteUiModel> = persistentListOf(),
    val searchQuery: String = "",
    val appliedSearchQuery: String = "",
    val isDeleteModeActive: Boolean = false,
    val sortOrder: NoteSortOrder = NoteSortOrder.DATE_DESC,
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
    data object OnSearchClicked : NotesEvent
    data class OnSortClicked(val sortOrder: NoteSortOrder) : NotesEvent
}

sealed interface NotesEffect {
    data class NavigateToEditor(val noteId: Long? = null) : NotesEffect
}