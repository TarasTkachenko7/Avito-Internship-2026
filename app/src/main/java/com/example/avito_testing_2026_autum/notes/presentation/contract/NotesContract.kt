package com.example.avito_testing_2026_autum.notes.presentation.contract

import androidx.compose.runtime.Immutable
import com.example.avito_testing_2026_autum.notes.domain.model.NoteSortOrder
import com.example.avito_testing_2026_autum.notes.presentation.models.NoteUiModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class NotesUiState(
    val isLoading: Boolean = true,
    val notes: ImmutableList<NoteUiModel> = persistentListOf(),
    val appliedSearchQuery: String = "",
    val isDeleteModeActive: Boolean = false,
    val sortOrder: NoteSortOrder = NoteSortOrder.DATE_DESC,
    val noteIdToDelete: Long? = null
)

sealed interface NotesEvent {

    sealed interface Query : NotesEvent {
        data class SearchSubmit(val query: String) : Query
        data object SearchClear : Query
        data class SortSelected(val sortOrder: NoteSortOrder) : Query
    }

    sealed interface Action : NotesEvent {
        data class DeleteIconClicked(val id: Long) : Action
        data class NoteCardClicked(val id: Long) : Action
        data object CreateNoteClicked : Action
        data object ToggleDeleteMode : Action
    }

    sealed interface Modal : NotesEvent {
        data object ConfirmDelete : Modal
        data object DismissDeleteDialog : Modal
    }
}

sealed interface NotesEffect {
    data class NavigateToEditor(val noteId: Long? = null) : NotesEffect
}