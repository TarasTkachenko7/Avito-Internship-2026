package com.example.avito_testing_2026_autum.tasks.presentation

import androidx.lifecycle.SavedStateHandle
import com.example.avito_testing_2026_autum.ai.domain.usecases.FormulateTaskUseCase
import com.example.avito_testing_2026_autum.tasks.domain.model.Task
import com.example.avito_testing_2026_autum.tasks.domain.model.TaskFilterType
import com.example.avito_testing_2026_autum.tasks.domain.model.TaskSortOrder
import com.example.avito_testing_2026_autum.tasks.domain.usecases.DeleteTaskByIdUseCase
import com.example.avito_testing_2026_autum.tasks.domain.usecases.GetTasksUseCase
import com.example.avito_testing_2026_autum.tasks.domain.usecases.ToggleTaskStatusUseCase
import com.example.avito_testing_2026_autum.tasks.domain.usecases.UpdateTaskTitleUseCase
import com.example.avito_testing_2026_autum.tasks.domain.usecases.UpsertTaskUseCase
import com.example.avito_testing_2026_autum.tasks.presentation.contract.TasksEvent
import com.example.avito_testing_2026_autum.tasks.presentation.contract.TasksModalOverlay
import com.example.avito_testing_2026_autum.tasks.presentation.contract.TasksUiState
import com.example.avito_testing_2026_autum.voice.domain.model.VoiceState
import com.example.avito_testing_2026_autum.voice.domain.recognition.SpeechRecognizerContract
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TasksViewModelTest {
    private val getTasks = mockk<GetTasksUseCase>()
    private val upsertTask = mockk<UpsertTaskUseCase>()
    private val toggleStatus = mockk<ToggleTaskStatusUseCase>(relaxed = true)
    private val updateTitle = mockk<UpdateTaskTitleUseCase>(relaxed = true)
    private val deleteTask = mockk<DeleteTaskByIdUseCase>(relaxed = true)
    private val formulateTask = mockk<FormulateTaskUseCase>(relaxed = true)
    private val speechRecognizer = mockk<SpeechRecognizerContract>(relaxed = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        every { getTasks(any(), any(), any()) } returns flowOf(emptyList())
        every { speechRecognizer.voiceState } returns MutableStateFlow(VoiceState.Idle)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun creationDraftAndModeAreRestoredFromSavedStateHandle() = runTest {
        val handle = SavedStateHandle()
        val first = createViewModel(handle)
        first.handleEvent(TasksEvent.Creation.Start)
        first.handleEvent(TasksEvent.Creation.DraftChanged("draft text"))

        val restored = createViewModel(handle)

        assertTrue(restored.state.value.isCreatingTask)
        assertEquals("draft text", restored.state.value.creationDraft)
    }

    @Test
    fun repeatedSaveWhileInsertIsSuspendedStartsOnlyOneInsert() = runTest {
        val insertGate = CompletableDeferred<Unit>()
        coEvery { upsertTask(rawTitle = "one task") } coAnswers {
            insertGate.await()
            Result.success(Unit)
        }
        val viewModel = createViewModel(SavedStateHandle())
        viewModel.handleEvent(TasksEvent.Creation.Start)
        viewModel.handleEvent(TasksEvent.Creation.Save("one task"))
        viewModel.handleEvent(TasksEvent.Creation.Save("one task"))

        coVerify(exactly = 1) { upsertTask(rawTitle = "one task") }
        insertGate.complete(Unit)
        advanceUntilIdle()
    }

    @Test
    fun failedAiTaskInsertCanRetryWithoutCallingAiAgain() = runTest {
        coEvery { formulateTask("spoken text") } returns Result.success("formulated task")
        coEvery { upsertTask(rawTitle = "formulated task") } returnsMany listOf(
            Result.failure(IllegalStateException("database unavailable")),
            Result.success(Unit)
        )
        val viewModel = createViewModel(SavedStateHandle())
        viewModel.handleEvent(TasksEvent.Modal.StartVoice)
        (speechRecognizer.voiceState as MutableStateFlow).value = VoiceState.Success("spoken text")
        advanceUntilIdle()

        assertEquals(TasksModalOverlay.AiSaveFailed, viewModel.state.value.currentOverlay)
        viewModel.handleEvent(TasksEvent.Modal.RetryAiSave)
        advanceUntilIdle()

        assertEquals(null, viewModel.state.value.currentOverlay)
        coVerify(exactly = 1) { formulateTask("spoken text") }
        coVerify(exactly = 2) { upsertTask(rawTitle = "formulated task") }
    }

    @Test
    fun blankAiResultIsNotSaved() = runTest {
        coEvery { formulateTask("spoken text") } returns Result.success("  ")
        val viewModel = createViewModel(SavedStateHandle())
        viewModel.handleEvent(TasksEvent.Modal.StartVoice)
        (speechRecognizer.voiceState as MutableStateFlow).value = VoiceState.Success("spoken text")
        advanceUntilIdle()

        coVerify(exactly = 0) { upsertTask(any(), any(), any(), any()) }
        assertEquals(null, viewModel.state.value.currentOverlay)
    }

    @Test
    fun aiFailureDoesNotPersistATask() = runTest {
        coEvery { formulateTask("spoken text") } returns Result.failure(IllegalStateException("AI unavailable"))
        val viewModel = createViewModel(SavedStateHandle())
        viewModel.handleEvent(TasksEvent.Modal.StartVoice)
        (speechRecognizer.voiceState as MutableStateFlow).value = VoiceState.Success("spoken text")
        advanceUntilIdle()

        coVerify(exactly = 0) { upsertTask(any(), any(), any(), any()) }
        assertEquals(null, viewModel.state.value.currentOverlay)
    }

    @Test
    fun lateVoiceSuccessAfterDismissDoesNotStartAiRequest() = runTest {
        val voiceState = speechRecognizer.voiceState as MutableStateFlow
        val viewModel = createViewModel(SavedStateHandle())
        viewModel.handleEvent(TasksEvent.Modal.StartVoice)
        viewModel.handleEvent(TasksEvent.Modal.DismissCurrentModal)
        voiceState.value = VoiceState.Success("late spoken text")
        advanceUntilIdle()

        coVerify(exactly = 0) { formulateTask(any()) }
        coVerify(exactly = 0) { upsertTask(any(), any(), any(), any()) }
        assertEquals(null, viewModel.state.value.currentOverlay)
    }

    @Test
    fun dismissingVoiceFlowCancelsAiResultBeforeItCanBeSaved() = runTest {
        val aiGate = CompletableDeferred<Result<String>>()
        coEvery { formulateTask("spoken text") } coAnswers { aiGate.await() }
        val viewModel = createViewModel(SavedStateHandle())
        viewModel.handleEvent(TasksEvent.Modal.StartVoice)
        (speechRecognizer.voiceState as MutableStateFlow).value = VoiceState.Success("spoken text")
        runCurrent()
        viewModel.handleEvent(TasksEvent.Modal.DismissCurrentModal)
        aiGate.complete(Result.success("late result"))
        advanceUntilIdle()

        coVerify(exactly = 0) { upsertTask(any(), any(), any(), any()) }
        assertEquals(null, viewModel.state.value.currentOverlay)
    }

    @Test
    fun switchingFromEditToFabOrCreationClearsTheRestoredEditMode() = runTest {
        val handle = SavedStateHandle()
        val first = createViewModel(handle)
        first.handleEvent(TasksEvent.Edit.Start(42L))
        first.handleEvent(TasksEvent.Edit.DraftChanged(42L, "edited draft"))
        first.handleEvent(TasksEvent.Modal.FabClicked)

        val afterFab = createViewModel(handle)
        assertEquals(null, afterFab.state.value.editingTaskId)
        assertEquals(false, afterFab.state.value.isCreatingTask)

        afterFab.handleEvent(TasksEvent.Creation.Start)
        val afterStartCreation = createViewModel(handle)
        assertEquals(null, afterStartCreation.state.value.editingTaskId)
        assertTrue(afterStartCreation.state.value.isCreatingTask)
    }

    @Test
    fun editDraftIsRestoredAndDeleteModeClearsIt() = runTest {
        val handle = SavedStateHandle(
            mapOf("tasks.editingId" to 42L, "tasks.editingDraft" to "edited title")
        )
        val restored = createViewModel(handle)
        assertEquals(42L, restored.state.value.editingTaskId)
        assertEquals("edited title", restored.state.value.editingDraftTitle)

        restored.handleEvent(TasksEvent.Action.ToggleDeleteMode)
        val afterDeleteMode = createViewModel(handle)
        assertEquals(null, afterDeleteMode.state.value.editingTaskId)
        assertEquals("", afterDeleteMode.state.value.editingDraftTitle)
    }

    @Test
    fun queryFailureDoesNotShowResultsFromThePreviousQueryAndCanRetry() = runTest {
        every { getTasks("", any(), any()) } returns flowOf(
            listOf(Task(1L, "old result", false, 1L))
        )
        every { getTasks("new", any(), any()) } returns flow { throw IllegalStateException("read failed") }
        val viewModel = createViewModel(SavedStateHandle())
        awaitState(viewModel.state) { !it.isLoading }
        assertEquals(1, viewModel.state.value.tasks.size)

        viewModel.handleEvent(TasksEvent.Query.SearchSubmit("new"))
        awaitState(viewModel.state) { it.hasLoadError }

        assertTrue(viewModel.state.value.hasLoadError)
        assertTrue(viewModel.state.value.tasks.isEmpty())
        viewModel.handleEvent(TasksEvent.Query.Retry)
        awaitState(viewModel.state) { it.hasLoadError && !it.isLoading }
        verify(exactly = 2) { getTasks("new", TaskSortOrder.DATE_DESC, TaskFilterType.ALL) }
    }

    private suspend fun awaitState(state: StateFlow<TasksUiState>, predicate: (TasksUiState) -> Boolean) {
        withContext(Dispatchers.Default) {
            withTimeout(2_000) { state.first(predicate) }
        }
    }

    private fun createViewModel(handle: SavedStateHandle) = TasksViewModel(
        getTasks,
        upsertTask,
        toggleStatus,
        updateTitle,
        deleteTask,
        formulateTask,
        speechRecognizer,
        handle
    )
}
