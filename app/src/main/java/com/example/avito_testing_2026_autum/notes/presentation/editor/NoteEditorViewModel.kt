package com.example.avito_testing_2026_autum.notes.presentation.editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.avito_testing_2026_autum.app.navigation.Screen
import com.example.avito_testing_2026_autum.notes.domain.model.Note
import com.example.avito_testing_2026_autum.notes.domain.usecases.editor.CreateTempImageFileUseCase
import com.example.avito_testing_2026_autum.notes.domain.usecases.editor.GetNoteByIdUseCase
import com.example.avito_testing_2026_autum.notes.domain.usecases.editor.SaveImageUseCase
import com.example.avito_testing_2026_autum.notes.domain.usecases.editor.UpsertNoteUseCase
import com.example.avito_testing_2026_autum.notes.presentation.contract.editor.NoteEditorEffect
import com.example.avito_testing_2026_autum.notes.presentation.contract.editor.NoteEditorEvent
import com.example.avito_testing_2026_autum.notes.presentation.contract.editor.NoteEditorUiState
import com.example.avito_testing_2026_autum.voice.domain.model.VoiceState
import com.example.avito_testing_2026_autum.voice.domain.recognition.SpeechRecognizerContract
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NoteEditorViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val getNoteByIdUseCase: GetNoteByIdUseCase,
    private val upsertNoteUseCase: UpsertNoteUseCase,
    private val saveImageUseCase: SaveImageUseCase,
    private val createTempImageFileUseCase: CreateTempImageFileUseCase,
    private val speechRecognizer: SpeechRecognizerContract
) : ViewModel() {

    private val noteId: Long? = savedStateHandle.toRoute<Screen.NoteEditor>().noteId

    private val _state = MutableStateFlow(NoteEditorUiState())
    val state: StateFlow<NoteEditorUiState> = _state.asStateFlow()

    private val _effect = Channel<NoteEditorEffect>(
        capacity = 1,
        onBufferOverflow = BufferOverflow.DROP_LATEST
    )
    val effect = _effect.receiveAsFlow()

    private var originalCreatedAt: Long = 0L
    private var tempCameraPath: String? = null

    init {
        observeVoiceState()
        if (noteId != null) {
            loadNote(noteId)
        } else {
            _state.update { it.copy(isLoading = false) }
        }
    }

    private fun observeVoiceState() {
        speechRecognizer.voiceState
            .onEach { voiceState ->
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
                                voiceState = voiceState,
                                showVoiceDialog = false
                            )
                        }
                        speechRecognizer.reset()
                    }
                    else -> {
                        _state.update { it.copy(voiceState = voiceState) }
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun loadNote(id: Long) {
        viewModelScope.launch {
            val note = getNoteByIdUseCase(id)
            note?.let { loadedNote ->
                originalCreatedAt = loadedNote.createdAt
                _state.update {
                    it.copy(
                        title = loadedNote.title,
                        text = loadedNote.text,
                        imageUri = loadedNote.imageUri,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun handleEvent(event: NoteEditorEvent) {
        when (event) {
            is NoteEditorEvent.OnTitleChanged -> _state.update { it.copy(title = event.title) }
            is NoteEditorEvent.OnTextChanged -> _state.update { it.copy(text = event.text) }
            is NoteEditorEvent.OnAttachmentClicked -> _state.update { it.copy(showAttachmentDialog = true) }
            is NoteEditorEvent.OnDismissAttachmentDialog -> _state.update { it.copy(showAttachmentDialog = false) }
            is NoteEditorEvent.OnGalleryClicked -> _state.update { it.copy(showAttachmentDialog = false) }
            is NoteEditorEvent.OnCameraClicked -> {
                _state.update { it.copy(showAttachmentDialog = false) }
                prepareCamera()
            }
            is NoteEditorEvent.OnImagePicked -> processAndSaveImage(event.uriString)
            is NoteEditorEvent.OnCameraCaptureSuccess -> {
                tempCameraPath?.let { path ->
                    processAndSaveImage(path)
                    tempCameraPath = null
                }
            }
            is NoteEditorEvent.OnRemoveImageClicked -> _state.update { it.copy(imageUri = null) }
            is NoteEditorEvent.OnSaveClicked -> saveNote()
            is NoteEditorEvent.OnBackClicked -> _effect.trySend(NoteEditorEffect.NavigateBack)
            
            is NoteEditorEvent.OnVoiceInputClicked -> {
                _state.update { it.copy(showVoiceDialog = true) }
                speechRecognizer.startListening()
            }
            is NoteEditorEvent.OnStopVoiceListening -> {
                speechRecognizer.stopListening()
            }
            is NoteEditorEvent.OnDismissVoiceDialog -> {
                speechRecognizer.cancel()
                _state.update { it.copy(showVoiceDialog = false) }
            }
            is NoteEditorEvent.OnPermissionDenied -> {
                _effect.trySend(NoteEditorEffect.ShowMessage("Для голосового ввода требуется доступ к микрофону"))
            }
        }
    }

    private fun prepareCamera() {
        viewModelScope.launch {
            val tempFile = createTempImageFileUseCase()
            tempCameraPath = tempFile.absolutePath
            _effect.trySend(NoteEditorEffect.LaunchCamera(tempFile.absolutePath))
        }
    }

    private fun processAndSaveImage(sourceUri: String) {
        viewModelScope.launch {
            val permanentPath = saveImageUseCase(sourceUri)
            _state.update { it.copy(imageUri = permanentPath) }
        }
    }

    private fun saveNote() {
        val currentState = state.value
        if (!currentState.isSaveButtonEnabled) return
        viewModelScope.launch {
            val noteToSave = Note(
                id = noteId ?: 0L,
                title = currentState.title.trim(),
                text = currentState.text.trim(),
                imageUri = currentState.imageUri,
                createdAt = if (noteId == null) System.currentTimeMillis() else originalCreatedAt
            )
            upsertNoteUseCase(noteToSave)
            _effect.trySend(NoteEditorEffect.NavigateBack)
        }
    }

    override fun onCleared() {
        super.onCleared()
        speechRecognizer.cancel()
    }
}