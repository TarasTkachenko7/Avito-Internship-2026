package com.example.avito_testing_2026_autum.notes.presentation.contract.editor

data class NoteEditorUiState(
    val isLoading: Boolean = true,
    val title: String = "",
    val text: String = "",
    val imageUri: String? = null,
    val showAttachmentDialog: Boolean = false
) {
    val isSaveButtonEnabled: Boolean
        get() = title.isNotBlank()
}

sealed interface NoteEditorEvent {
    data class OnTitleChanged(val title: String) : NoteEditorEvent
    data class OnTextChanged(val text: String) : NoteEditorEvent
    data object OnAttachmentClicked : NoteEditorEvent
    data object OnDismissAttachmentDialog : NoteEditorEvent
    data object OnGalleryClicked : NoteEditorEvent
    data object OnCameraClicked : NoteEditorEvent
    data class OnImagePicked(val uriString: String) : NoteEditorEvent
    data object OnCameraCaptureSuccess : NoteEditorEvent
    data object OnRemoveImageClicked : NoteEditorEvent
    data object OnSaveClicked : NoteEditorEvent
    data object OnBackClicked : NoteEditorEvent
}

sealed interface NoteEditorEffect {
    data object NavigateBack : NoteEditorEffect
    data class LaunchCamera(val uriString: String) : NoteEditorEffect
}