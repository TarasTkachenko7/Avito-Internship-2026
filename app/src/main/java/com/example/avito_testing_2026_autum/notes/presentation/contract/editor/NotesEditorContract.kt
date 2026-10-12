package com.example.avito_testing_2026_autum.notes.presentation.contract.editor

import androidx.compose.runtime.Immutable
import com.example.avito_testing_2026_autum.core.utils.UiText
import com.example.avito_testing_2026_autum.voice.domain.model.VoiceState

@Immutable
sealed interface NoteEditorOverlay {
    data object AttachmentOptions : NoteEditorOverlay
    data class VoiceInput(val voiceState: VoiceState = VoiceState.Idle) : NoteEditorOverlay
}

@Immutable
data class NoteEditorUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val title: String = "",
    val text: String = "",
    val imageUri: String? = null,

    val tempCameraUri: String? = null,
    val originalCreatedAt: Long? = null,

    val currentOverlay: NoteEditorOverlay? = null
) {
    val isSaveButtonEnabled: Boolean
        get() = title.isNotBlank() && !isSaving
}

sealed interface NoteEditorEvent {

    sealed interface Input : NoteEditorEvent {
        data class TitleChanged(val title: String) : Input
        data class TextChanged(val text: String) : Input
    }

    sealed interface Action : NoteEditorEvent {
        data object SaveClicked : Action
        data object BackClicked : Action
        data object AttachmentClicked : Action
        data object RemoveImageClicked : Action
    }

    sealed interface Dialog : NoteEditorEvent {
        data object GalleryClicked : Dialog
        data object CameraClicked : Dialog
        data object DismissOverlay : Dialog
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