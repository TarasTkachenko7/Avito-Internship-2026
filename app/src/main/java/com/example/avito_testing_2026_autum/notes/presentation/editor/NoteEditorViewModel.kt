package com.example.avito_testing_2026_autum.notes.presentation.editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.avito_testing_2026_autum.R
import com.example.avito_testing_2026_autum.app.navigation.Screen
import com.example.avito_testing_2026_autum.core.utils.UiText
import com.example.avito_testing_2026_autum.notes.domain.usecases.editor.CreateTempImageFileUseCase
import com.example.avito_testing_2026_autum.notes.domain.usecases.editor.DeleteImageUseCase
import com.example.avito_testing_2026_autum.notes.domain.usecases.editor.GetNoteByIdUseCase
import com.example.avito_testing_2026_autum.notes.domain.usecases.editor.SaveImageUseCase
import com.example.avito_testing_2026_autum.notes.domain.usecases.editor.UpsertNoteUseCase
import com.example.avito_testing_2026_autum.notes.presentation.contract.editor.NoteEditorEffect
import com.example.avito_testing_2026_autum.notes.presentation.contract.editor.NoteEditorEvent
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
import kotlinx.coroutines.Job
import kotlin.coroutines.cancellation.CancellationException

class NoteEditorViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val getNoteByIdUseCase: GetNoteByIdUseCase,
    private val upsertNoteUseCase: UpsertNoteUseCase,
    private val saveImageUseCase: SaveImageUseCase,
    private val deleteImageUseCase: DeleteImageUseCase,
    private val createTempImageFileUseCase: CreateTempImageFileUseCase,
    private val speechRecognizer: SpeechRecognizerContract
) : ViewModel() {

    private val noteId: Long? = savedStateHandle.toRoute<Screen.NoteEditor>().noteId

    companion object {
        private const val KEY_TITLE = "state_title"
        private const val KEY_TEXT = "state_text"
        private const val KEY_TEMP_URI = "state_temp_uri"
        private const val KEY_IMAGE_URI = "state_image_uri"
        private const val KEY_PENDING_IMAGE_SOURCE = "state_pending_image_source"

        private const val KEY_ORIGINAL_TITLE = "state_orig_title"
        private const val KEY_ORIGINAL_TEXT = "state_orig_text"
        private const val KEY_ORIGINAL_IMAGE_URI = "state_orig_image_uri"
        private const val KEY_ORIGINAL_CREATED_AT = "state_orig_created_at"
    }

    private val _state = MutableStateFlow(
        NoteEditorUiState(
            title = savedStateHandle[KEY_TITLE] ?: "",
            text = savedStateHandle[KEY_TEXT] ?: "",
            tempCameraUri = savedStateHandle[KEY_TEMP_URI],
            imageUri = savedStateHandle[KEY_IMAGE_URI],
            originalTitle = savedStateHandle[KEY_ORIGINAL_TITLE] ?: "",
            originalText = savedStateHandle[KEY_ORIGINAL_TEXT] ?: "",
            originalImageUri = savedStateHandle[KEY_ORIGINAL_IMAGE_URI],
            originalCreatedAt = savedStateHandle[KEY_ORIGINAL_CREATED_AT],
            isLoading = noteId != null && !savedStateHandle.contains(KEY_ORIGINAL_TITLE)
        )
    )
    val state: StateFlow<NoteEditorUiState> = _state.asStateFlow()

    private val _effect = Channel<NoteEditorEffect>(capacity = Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        observeVoiceState()
        initNoteData()
        if (noteId == null || savedStateHandle.contains(KEY_ORIGINAL_TITLE)) resumePendingImageImport()
    }

    private var imageImportJob: Job? = null

    private fun initNoteData() {
        if (noteId == null || savedStateHandle.contains(KEY_ORIGINAL_TITLE)) {
            return
        }

        viewModelScope.launch {
            loadNote()
        }
    }

    private suspend fun loadNote() {
        try {
            val note = getNoteByIdUseCase(noteId ?: return)
            if (note != null) {
                savedStateHandle[KEY_ORIGINAL_TITLE] = note.title
                savedStateHandle[KEY_ORIGINAL_TEXT] = note.text
                savedStateHandle[KEY_ORIGINAL_IMAGE_URI] = note.imageUri
                savedStateHandle[KEY_ORIGINAL_CREATED_AT] = note.createdAt

                val currentTitle = savedStateHandle[KEY_TITLE] ?: note.title
                val currentText = savedStateHandle[KEY_TEXT] ?: note.text
                val currentImageUri = savedStateHandle[KEY_IMAGE_URI] ?: note.imageUri

                savedStateHandle[KEY_TITLE] = currentTitle
                savedStateHandle[KEY_TEXT] = currentText
                savedStateHandle[KEY_IMAGE_URI] = currentImageUri

                _state.update {
                    it.copy(
                        title = currentTitle,
                        text = currentText,
                        imageUri = currentImageUri,
                        originalTitle = note.title,
                        originalText = note.text,
                        originalImageUri = note.imageUri,
                        originalCreatedAt = note.createdAt,
                        isLoading = false,
                        hasLoadError = false
                    )
                }
                resumePendingImageImport()
            } else {
                _state.update { it.copy(isLoading = false, hasLoadError = true) }
                sendEffect(NoteEditorEffect.ShowMessage(UiText.StringResource(R.string.note_editor_error_not_found)))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            _state.update { it.copy(isLoading = false, hasLoadError = true) }
            sendEffect(NoteEditorEffect.ShowMessage(UiText.StringResource(R.string.note_editor_error_load)))
        }
    }

    private fun observeVoiceState() {
        viewModelScope.launch {
            speechRecognizer.voiceState.collect { newVoiceState ->
                when (newVoiceState) {
                    is VoiceState.Success -> {
                        if (!_state.value.showVoiceDialog) return@collect
                        val currentText = _state.value.text
                        val appendedText =
                            if (currentText.isBlank()) newVoiceState.text else "$currentText ${newVoiceState.text}"
                        savedStateHandle[KEY_TEXT] = appendedText

                        _state.update {
                            it.copy(
                                text = appendedText,
                                showVoiceDialog = false,
                                voiceState = VoiceState.Idle
                            )
                        }
                        speechRecognizer.reset()
                    }

                    else -> {
                        if (_state.value.showVoiceDialog) {
                            _state.update { it.copy(voiceState = newVoiceState) }
                        }
                    }
                }
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
            is NoteEditorEvent.Input.TitleChanged -> {
                savedStateHandle[KEY_TITLE] = event.title
                _state.update { it.copy(title = event.title) }
            }

            is NoteEditorEvent.Input.TextChanged -> {
                savedStateHandle[KEY_TEXT] = event.text
                _state.update { it.copy(text = event.text) }
            }
        }
    }

    private fun handleAction(event: NoteEditorEvent.Action) {
        when (event) {
            is NoteEditorEvent.Action.RetryLoading -> {
                _state.update { it.copy(isLoading = true, hasLoadError = false) }
                viewModelScope.launch { loadNote() }
            }
            is NoteEditorEvent.Action.RemoveImageClicked -> {
                val currentImage = _state.value.imageUri
                imageImportJob?.cancel()
                imageImportJob = null
                savedStateHandle.remove<String>(KEY_PENDING_IMAGE_SOURCE)
                savedStateHandle.remove<String>(KEY_IMAGE_URI)
                _state.update { it.copy(imageUri = null, isImageProcessing = false) }
                if (currentImage != null && currentImage != _state.value.originalImageUri) deleteImage(currentImage)
            }

            is NoteEditorEvent.Action.BackClicked -> {
                if (_state.value.hasChanges) {
                    _state.update { it.copy(showUnsavedChangesDialog = true) }
                } else {
                    sendEffect(NoteEditorEffect.NavigateBack)
                }
            }

            is NoteEditorEvent.Action.DiscardAndExit -> {
                _state.update { it.copy(showUnsavedChangesDialog = false) }
                discardDraftFiles()
                sendEffect(NoteEditorEffect.NavigateBack)
            }

            is NoteEditorEvent.Action.SaveClicked -> saveNote()
        }
    }

    private fun handleDialog(event: NoteEditorEvent.Dialog) {
        when (event) {
            is NoteEditorEvent.Dialog.CameraClicked -> prepareCamera()
            is NoteEditorEvent.Dialog.DismissVoiceDialog -> {
                speechRecognizer.cancel()
                _state.update { it.copy(showVoiceDialog = false, voiceState = VoiceState.Idle) }
            }

            is NoteEditorEvent.Dialog.DismissUnsavedDialog -> {
                _state.update { it.copy(showUnsavedChangesDialog = false) }
            }
        }
    }

    private fun handleMedia(event: NoteEditorEvent.Media) {
        when (event) {
            is NoteEditorEvent.Media.ImagePicked -> processAndSaveImage(event.uriString)
            is NoteEditorEvent.Media.CameraCaptureSuccess -> {
                _state.value.tempCameraUri?.let { path ->
                    processAndSaveImage(path)
                }
            }
            is NoteEditorEvent.Media.CameraCaptureCancelled -> {
                _state.value.tempCameraUri?.let(::deleteImage)
                savedStateHandle.remove<String>(KEY_TEMP_URI)
                _state.update { it.copy(tempCameraUri = null) }
            }
        }
    }

    private fun handleVoice(event: NoteEditorEvent.Voice) {
        when (event) {
            is NoteEditorEvent.Voice.VoiceInputClicked -> {
                _state.update { it.copy(showVoiceDialog = true) }
                speechRecognizer.startListening()
            }

            is NoteEditorEvent.Voice.StopVoiceListening -> {
                speechRecognizer.stopListening()
            }

            is NoteEditorEvent.Voice.PermissionDenied -> {
                speechRecognizer.cancel()
                _state.update { it.copy(showVoiceDialog = false) }
                sendEffect(NoteEditorEffect.ShowMessage(UiText.StringResource(R.string.voice_error_permission_denied)))
            }
        }
    }

    private fun prepareCamera() {
        viewModelScope.launch {
            try {
                val tempFile = createTempImageFileUseCase()
                val absolutePath = tempFile.absolutePath
                savedStateHandle[KEY_TEMP_URI] = absolutePath
                _state.update { it.copy(tempCameraUri = absolutePath) }
                sendEffect(NoteEditorEffect.LaunchCamera(absolutePath))
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                sendEffect(NoteEditorEffect.ShowMessage(UiText.StringResource(R.string.note_editor_error_camera_prep)))
            }
        }
    }

    private fun processAndSaveImage(sourceUri: String) {
        savedStateHandle[KEY_PENDING_IMAGE_SOURCE] = sourceUri
        imageImportJob?.cancel()
        _state.update { it.copy(isImageProcessing = true) }
        imageImportJob = viewModelScope.launch {
            try {
                val permanentPath = saveImageUseCase(sourceUri)
                if (permanentPath != null) {
                    val previousImage = _state.value.imageUri
                    savedStateHandle[KEY_IMAGE_URI] = permanentPath
                    savedStateHandle.remove<String>(KEY_PENDING_IMAGE_SOURCE)
                    if (savedStateHandle.get<String>(KEY_TEMP_URI) == sourceUri) {
                        savedStateHandle.remove<String>(KEY_TEMP_URI)
                        _state.update { it.copy(tempCameraUri = null) }
                    }
                    _state.update { it.copy(imageUri = permanentPath, isImageProcessing = false) }
                    if (previousImage != null && previousImage != _state.value.originalImageUri && previousImage != permanentPath) {
                        deleteImage(previousImage)
                    }
                    if (sourceUri.startsWith("/")) deleteImage(sourceUri)
                } else {
                    _state.update { it.copy(isImageProcessing = false) }
                    sendEffect(NoteEditorEffect.ShowMessage(UiText.StringResource(R.string.note_editor_error_image_save)))
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _state.update { it.copy(isImageProcessing = false) }
                sendEffect(NoteEditorEffect.ShowMessage(UiText.StringResource(R.string.note_editor_error_image_save)))
            }
        }
    }

    private fun resumePendingImageImport() {
        savedStateHandle.get<String>(KEY_PENDING_IMAGE_SOURCE)?.let(::processAndSaveImage)
    }

    private fun deleteImage(uri: String) {
        viewModelScope.launch {
            try { deleteImageUseCase(uri) } catch (e: CancellationException) { throw e } catch (_: Exception) { }
        }
    }

    private fun discardDraftFiles() {
        imageImportJob?.cancel()
        imageImportJob = null
        val current = _state.value.imageUri
        if (current != null && current != _state.value.originalImageUri) deleteImage(current)
        _state.value.tempCameraUri?.let(::deleteImage)
        savedStateHandle.remove<String>(KEY_TEMP_URI)
        savedStateHandle.remove<String>(KEY_PENDING_IMAGE_SOURCE)
    }

    private fun saveNote() {
        val currentState = state.value
        if (!currentState.isSaveButtonEnabled) return

        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            try {
                upsertNoteUseCase(
                    id = noteId ?: 0L,
                    rawTitle = currentState.title,
                    rawText = currentState.text,
                    imageUri = currentState.imageUri,
                    createdAt = currentState.originalCreatedAt
                ).onSuccess {
                    val oldImage = currentState.originalImageUri
                    if (oldImage != null && oldImage != currentState.imageUri) deleteImage(oldImage)
                    _state.value.tempCameraUri?.let(::deleteImage)
                    savedStateHandle.remove<String>(KEY_TEMP_URI)
                    sendEffect(NoteEditorEffect.NavigateBack)
                }.onFailure {
                    _state.update { it.copy(isSaving = false) }
                    sendEffect(NoteEditorEffect.ShowMessage(UiText.StringResource(R.string.note_editor_error_note_save)))
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
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
