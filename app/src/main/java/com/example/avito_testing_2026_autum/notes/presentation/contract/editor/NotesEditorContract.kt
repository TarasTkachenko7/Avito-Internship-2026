package com.example.avito_testing_2026_autum.notes.presentation.contract.editor

import androidx.compose.runtime.Immutable
import com.example.avito_testing_2026_autum.core.utils.UiText
import com.example.avito_testing_2026_autum.voice.domain.model.VoiceState

@Immutable
data class NoteEditorUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,

    val title: String = "",
    val text: String = "",
    val imageUri: String? = null,
    val tempCameraUri: String? = null,

    val originalTitle: String = "",
    val originalText: String = "",
    val originalImageUri: String? = null,
    val originalCreatedAt: Long? = null,

    val showVoiceDialog: Boolean = false,
    val voiceState: VoiceState = VoiceState.Idle,
    val showUnsavedChangesDialog: Boolean = false,
) {
    val hasChanges: Boolean
        get() = title.trim() != originalTitle.trim() ||
                text.trim() != originalText.trim() ||
                imageUri != originalImageUri

    val isSaveButtonEnabled: Boolean
        get() = title.isNotBlank() && !isSaving && hasChanges
}

sealed interface NoteEditorEvent {

    sealed interface Input : NoteEditorEvent {
        data class TitleChanged(val title: String) : Input
        data class TextChanged(val text: String) : Input
    }

    sealed interface Action : NoteEditorEvent {
        data object SaveClicked : Action
        data object BackClicked : Action
        data object RemoveImageClicked : Action
        data object DiscardAndExit : Action
    }

    sealed interface Dialog : NoteEditorEvent {
        data object CameraClicked : Dialog
        data object DismissVoiceDialog : Dialog
        data object DismissUnsavedDialog : Dialog
    }

    sealed interface Media : NoteEditorEvent {
        data class ImagePicked(val uriString: String) : Media
        data object CameraCaptureSuccess : Media
    }

    sealed interface Voice : NoteEditorEvent {
        data object VoiceInputClicked : Voice
        data object StopVoiceListening : Voice
        data object PermissionDenied : Voice
    }

}

sealed interface NoteEditorEffect {
    data object NavigateBack : NoteEditorEffect
    data class LaunchCamera(val uriString: String) : NoteEditorEffect
    data object RequestMicrophonePermission : NoteEditorEffect
    data class ShowMessage(val message: UiText) : NoteEditorEffect
}