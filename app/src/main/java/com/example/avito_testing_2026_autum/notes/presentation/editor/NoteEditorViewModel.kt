package com.example.avito_testing_2026_autum.notes.presentation.editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.avito_testing_2026_autum.app.navigation.Screen
import com.example.avito_testing_2026_autum.core.utils.UiText
import com.example.avito_testing_2026_autum.notes.domain.usecases.editor.CreateTempImageFileUseCase
import com.example.avito_testing_2026_autum.notes.domain.usecases.editor.GetNoteByIdUseCase
import com.example.avito_testing_2026_autum.notes.domain.usecases.editor.SaveImageUseCase
import com.example.avito_testing_2026_autum.notes.domain.usecases.editor.UpsertNoteUseCase
import com.example.avito_testing_2026_autum.notes.presentation.contract.editor.NoteEditorEffect
import com.example.avito_testing_2026_autum.notes.presentation.contract.editor.NoteEditorEvent
import com.example.avito_testing_2026_autum.notes.presentation.contract.editor.NoteEditorOverlay
import com.example.avito_testing_2026_autum.notes.presentation.contract.editor.NoteEditorUiState
import com.example.avito_testing_2026_autum.voice.domain.model.VoiceState
import com.example.avito_testing_2026_autum.voice.domain.recognition.SpeechRecognizerContract
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.avito_testing_2026_autum.R

class NoteEditorViewModel(
    savedStateHandle: SavedStateHandle,
    private val getNoteByIdUseCase: GetNoteByIdUseCase,
    private val upsertNoteUseCase: UpsertNoteUseCase,
    private val saveImageUseCase: SaveImageUseCase,
    private val createTempImageFileUseCase: CreateTempImageFileUseCase,
    private val speechRecognizer: SpeechRecognizerContract
) : ViewModel() {

    private val noteId: Long? = savedStateHandle.toRoute<Screen.NoteEditor>().noteId

    private val _state = MutableStateFlow(NoteEditorUiState())
    val state: StateFlow<NoteEditorUiState> = _state.asStateFlow()

    private val _effect = Channel<NoteEditorEffect>(capacity = Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        observeVoiceState()
        if (noteId != null) {
            loadNote(noteId)
        } else {
            _state.update { it.copy(isLoading = false) }
        }
    }

    private fun observeVoiceState() {
        viewModelScope.launch {
            speechRecognizer.voiceState.collect { voiceState ->
                when (voiceState) {
                    is VoiceState.Success -> {
                        val currentText = _state.value.text
                        val appendedText = if (currentText.isBlank()) {
                            voiceState.text
                        } else {
                            "$currentText ${voiceState.text}"
                        }
                        _state.update {
                            it.copy(
                                text = appendedText,
                                currentOverlay = null
                            )
                        }
                        speechRecognizer.reset()
                    }

                    else -> {
                        val currentOverlay = _state.value.currentOverlay
                        if (currentOverlay is NoteEditorOverlay.VoiceInput) {
                            _state.update {
                                it.copy(currentOverlay = NoteEditorOverlay.VoiceInput(voiceState))
                            }
                        }
                    }
                }
            }
        }
    }

    private fun loadNote(id: Long) {
        viewModelScope.launch {
            val note = getNoteByIdUseCase(id)
            if (note != null) {
                _state.update {
                    it.copy(
                        title = note.title,
                        text = note.text,
                        imageUri = note.imageUri,
                        originalCreatedAt = note.createdAt,
                        isLoading = false
                    )
                }
            } else {
                _state.update { it.copy(isLoading = false) }
                sendEffect(NoteEditorEffect.ShowMessage(UiText.StringResource(R.string.note_editor_error_not_found)))
                sendEffect(NoteEditorEffect.NavigateBack)
            }
        }
    }

    fun handleEvent(event: NoteEditorEvent) {
        when (event) {
            is NoteEditorEvent.Input -> handleInput(event)
            is NoteEditorEvent.Action -> handleAction(event)
            is NoteEditorEvent.Dialog -> handleDialog(event)
            is NoteEditorEvent.Media -> handleMedia(event)
            is NoteEditorEvent.Voice -> handleVoice(event)
        }
    }

    private fun handleInput(event: NoteEditorEvent.Input) {
        when (event) {
            is NoteEditorEvent.Input.TitleChanged -> _state.update { it.copy(title = event.title) }
            is NoteEditorEvent.Input.TextChanged -> _state.update { it.copy(text = event.text) }
        }
    }

    private fun handleAction(event: NoteEditorEvent.Action) {
        when (event) {
            is NoteEditorEvent.Action.AttachmentClicked -> {
                _state.update { it.copy(currentOverlay = NoteEditorOverlay.AttachmentOptions) }
            }

            is NoteEditorEvent.Action.RemoveImageClicked -> {
                _state.update { it.copy(imageUri = null) }
            }

            is NoteEditorEvent.Action.SaveClicked -> saveNote()
            is NoteEditorEvent.Action.BackClicked -> sendEffect(NoteEditorEffect.NavigateBack)
        }
    }

    private fun handleDialog(event: NoteEditorEvent.Dialog) {
        when (event) {
            is NoteEditorEvent.Dialog.DismissOverlay -> {
                _state.update { it.copy(currentOverlay = null) }
            }

            is NoteEditorEvent.Dialog.GalleryClicked -> {
                _state.update { it.copy(currentOverlay = null) }
            }

            is NoteEditorEvent.Dialog.CameraClicked -> {
                _state.update { it.copy(currentOverlay = null) }
                prepareCamera()
            }
        }
    }

    private fun handleMedia(event: NoteEditorEvent.Media) {
        when (event) {
            is NoteEditorEvent.Media.ImagePicked -> processAndSaveImage(event.uriString)
            is NoteEditorEvent.Media.CameraCaptureSuccess -> {
                _state.value.tempCameraUri?.let { path ->
                    processAndSaveImage(path)
                    _state.update { it.copy(tempCameraUri = null) }
                }
            }
        }
    }

    private fun handleVoice(event: NoteEditorEvent.Voice) {
        when (event) {
            is NoteEditorEvent.Voice.VoiceInputClicked -> {
                _state.update { it.copy(currentOverlay = NoteEditorOverlay.VoiceInput()) }
                speechRecognizer.startListening()
            }

            is NoteEditorEvent.Voice.StopVoiceListening -> {
                speechRecognizer.stopListening()
            }

            is NoteEditorEvent.Voice.PermissionDenied -> {
                sendEffect(NoteEditorEffect.ShowMessage(UiText.StringResource(R.string.note_editor_error_mic_permission)))
            }
        }
    }

    private fun prepareCamera() {
        viewModelScope.launch {
            try {
                val tempFile = createTempImageFileUseCase()
                val absolutePath = tempFile.absolutePath
                _state.update { it.copy(tempCameraUri = absolutePath) }
                sendEffect(NoteEditorEffect.LaunchCamera(absolutePath))
            } catch (e: Exception) {
                sendEffect(NoteEditorEffect.ShowMessage(UiText.StringResource(R.string.note_editor_error_camera_prep)))
            }
        }
    }

    private fun processAndSaveImage(sourceUri: String) {
        viewModelScope.launch {
            val permanentPath = saveImageUseCase(sourceUri)
            if (permanentPath != null) {
                _state.update { it.copy(imageUri = permanentPath) }
            } else {
                sendEffect(NoteEditorEffect.ShowMessage(UiText.StringResource(R.string.note_editor_error_image_save)))
            }
        }
    }

    private fun saveNote() {
        val currentState = state.value
        if (!currentState.isSaveButtonEnabled) return

        _state.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            upsertNoteUseCase(
                id = noteId ?: 0L,
                rawTitle = currentState.title,
                rawText = currentState.text,
                imageUri = currentState.imageUri,
                createdAt = currentState.originalCreatedAt
            ).onSuccess {
                sendEffect(NoteEditorEffect.NavigateBack)
            }.onFailure {
                _state.update { it.copy(isSaving = false) }
                sendEffect(NoteEditorEffect.ShowMessage(UiText.StringResource(R.string.note_editor_error_note_save)))
            }
        }
    }

    private fun sendEffect(effect: NoteEditorEffect) {
        _effect.trySend(effect)
    }

    override fun onCleared() {
        super.onCleared()
        speechRecognizer.cancel()
    }
}