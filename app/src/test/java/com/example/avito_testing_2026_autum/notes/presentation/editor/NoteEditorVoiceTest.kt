package com.example.avito_testing_2026_autum.notes.presentation.editor

import androidx.lifecycle.SavedStateHandle
import com.example.avito_testing_2026_autum.notes.domain.usecases.editor.CreateTempImageFileUseCase
import com.example.avito_testing_2026_autum.notes.domain.usecases.editor.DeleteImageUseCase
import com.example.avito_testing_2026_autum.notes.domain.usecases.editor.GetNoteByIdUseCase
import com.example.avito_testing_2026_autum.notes.domain.usecases.editor.SaveImageUseCase
import com.example.avito_testing_2026_autum.notes.domain.usecases.editor.UpsertNoteUseCase
import com.example.avito_testing_2026_autum.notes.presentation.contract.editor.NoteEditorEvent
import com.example.avito_testing_2026_autum.voice.domain.model.VoiceState
import com.example.avito_testing_2026_autum.voice.domain.recognition.SpeechRecognizerContract
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NoteEditorVoiceTest {
    private val getNote = mockk<GetNoteByIdUseCase>(relaxed = true)
    private val upsertNote = mockk<UpsertNoteUseCase>(relaxed = true)
    private val saveImage = mockk<SaveImageUseCase>(relaxed = true)
    private val deleteImage = mockk<DeleteImageUseCase>(relaxed = true)
    private val createTempImage = mockk<CreateTempImageFileUseCase>(relaxed = true)
    private val speechRecognizer = mockk<SpeechRecognizerContract>(relaxed = true)
    private val voiceState = MutableStateFlow<VoiceState>(VoiceState.Idle)

    @Before
    fun setUp() {
        Dispatchers.setMain(kotlinx.coroutines.test.UnconfinedTestDispatcher())
        every { speechRecognizer.voiceState } returns voiceState
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun successAfterVoiceDialogWasDismissedDoesNotChangeNote() = runTest {
        val viewModel = NoteEditorViewModel(
            SavedStateHandle(), getNote, upsertNote, saveImage, deleteImage, createTempImage, speechRecognizer
        )
        viewModel.handleEvent(NoteEditorEvent.Input.TextChanged("existing text"))
        viewModel.handleEvent(NoteEditorEvent.Voice.VoiceInputClicked)
        viewModel.handleEvent(NoteEditorEvent.Dialog.DismissVoiceDialog)

        voiceState.value = VoiceState.Success("late transcript")
        advanceUntilIdle()

        assertEquals("existing text", viewModel.state.value.text)
        assertEquals(false, viewModel.state.value.showVoiceDialog)
    }
}
