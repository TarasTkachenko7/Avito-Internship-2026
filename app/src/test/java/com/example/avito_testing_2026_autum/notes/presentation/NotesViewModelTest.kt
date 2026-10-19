package com.example.avito_testing_2026_autum.notes.presentation

import com.example.avito_testing_2026_autum.notes.domain.model.NoteSortOrder
import com.example.avito_testing_2026_autum.notes.domain.usecases.DeleteNoteUseCase
import com.example.avito_testing_2026_autum.notes.domain.usecases.GetNotesUseCase
import com.example.avito_testing_2026_autum.notes.presentation.contract.NotesEffect
import com.example.avito_testing_2026_autum.notes.presentation.contract.NotesEvent
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NotesViewModelTest {
    private val getNotesUseCase = mockk<GetNotesUseCase>()
    private val deleteNoteUseCase = mockk<DeleteNoteUseCase>()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadFailureIsDistinctFromEmptyAndRetryKeepsAppliedQueryAndSort() = runTest {
        every { getNotesUseCase(any(), any()) } returns flow { throw IllegalStateException("read failed") }
        coEvery { deleteNoteUseCase(any()) } returns Result.success(Unit)

        val viewModel = NotesViewModel(getNotesUseCase, deleteNoteUseCase)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.hasLoadError)
        assertFalse(viewModel.state.value.isLoading)

        viewModel.handleEvent(NotesEvent.Query.SearchSubmit("  memo  "))
        advanceUntilIdle()
        viewModel.handleEvent(NotesEvent.Query.SortSelected(NoteSortOrder.DATE_ASC))
        advanceUntilIdle()
        viewModel.handleEvent(NotesEvent.Action.RetryLoading)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.hasLoadError)
        verify(exactly = 2) { getNotesUseCase("  memo  ", NoteSortOrder.DATE_ASC) }
    }

    @Test
    fun failedDeleteKeepsConfirmationAndEmitsMessage() = runTest {
        every { getNotesUseCase(any(), any()) } returns flowOf(emptyList())
        coEvery { deleteNoteUseCase(42L) } returns Result.failure(IllegalStateException("delete failed"))
        val viewModel = NotesViewModel(getNotesUseCase, deleteNoteUseCase)
        val effects = Channel<NotesEffect>(Channel.UNLIMITED)
        val collectJob = backgroundScope.launch { viewModel.effect.collect { effects.send(it) } }
        advanceUntilIdle()

        viewModel.handleEvent(NotesEvent.Action.DeleteIconClicked(42L))
        viewModel.handleEvent(NotesEvent.Modal.ConfirmDelete)
        advanceUntilIdle()

        assertEquals(42L, viewModel.state.value.noteIdToDelete)
        assertTrue(effects.receive() is NotesEffect.ShowMessage)
        collectJob.cancel()
    }
}
