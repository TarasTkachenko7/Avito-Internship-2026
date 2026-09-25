package com.example.avito_testing_2026_autum.notes.data.repository

import com.example.avito_testing_2026_autum.core.dispatchers.TestDispatchersProvider
import com.example.avito_testing_2026_autum.notes.data.local.NoteDao
import com.example.avito_testing_2026_autum.notes.data.local.NoteEntity
import com.example.avito_testing_2026_autum.notes.domain.model.Note
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class NoteRepositoryImplTest {

    private lateinit var dao: NoteDao

    private lateinit var repository: NoteRepositoryImpl

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        dao = mockk()
        val dispatchersProvider = TestDispatchersProvider(testDispatcher)
        repository = NoteRepositoryImpl(dao, dispatchersProvider)
    }

    @Test
    fun `getNoteById returns mapped note when dao returns entity`() = runTest(testDispatcher) {
        val noteId = 1L
        val entity = NoteEntity(
            id = noteId,
            title = "Test",
            text = "Content",
            imageUri = null,
            createdAt = 100L
        )
        val expectedNote =
            Note(id = noteId, title = "Test", text = "Content", imageUri = null, createdAt = 100L)

        coEvery { dao.getNoteById(noteId) } returns entity

        val actualNote = repository.getNoteById(noteId)

        assertEquals(expectedNote, actualNote)
        coVerify(exactly = 1) { dao.getNoteById(noteId) }
    }

    @Test
    fun `getNoteById returns null when dao returns null`() = runTest(testDispatcher) {
        val noteId = 1L
        coEvery { dao.getNoteById(noteId) } returns null

        val actualNote = repository.getNoteById(noteId)

        assertNull(actualNote)
    }

    @Test
    fun `getNotesDesc returns flow of mapped notes`() = runTest(testDispatcher) {
        val query = "Test"
        val entities = listOf(
            NoteEntity(id = 1L, title = "Test 1", text = null, imageUri = null, createdAt = 200L),
            NoteEntity(id = 2L, title = "Test 2", text = null, imageUri = null, createdAt = 100L)
        )
        val expectedNotes = listOf(
            Note(id = 1L, title = "Test 1", text = null, imageUri = null, createdAt = 200L),
            Note(id = 2L, title = "Test 2", text = null, imageUri = null, createdAt = 100L)
        )

        every { dao.getNotesDesc(query) } returns flowOf(entities)

        val actualNotes = repository.getNotesDesc(query).first()

        assertEquals(expectedNotes, actualNotes)
    }

    @Test
    fun `upsertNote maps domain to entity and calls dao`() = runTest(testDispatcher) {
        val note = Note(id = 1L, title = "Title", text = "Text", imageUri = null, createdAt = 100L)
        val expectedEntity =
            NoteEntity(id = 1L, title = "Title", text = "Text", imageUri = null, createdAt = 100L)

        coEvery { dao.upsertNote(any()) } returns Unit

        repository.upsertNote(note)

        coVerify(exactly = 1) { dao.upsertNote(expectedEntity) }
    }

    @Test
    fun `getNotesAsc returns flow of mapped notes`() = runTest(testDispatcher) {
        val query = "Test"
        val entities = listOf(
            NoteEntity(id = 2L, title = "Test 2", text = null, imageUri = null, createdAt = 100L),
            NoteEntity(id = 1L, title = "Test 1", text = null, imageUri = null, createdAt = 200L)
        )
        val expectedNotes = listOf(
            Note(id = 2L, title = "Test 2", text = null, imageUri = null, createdAt = 100L),
            Note(id = 1L, title = "Test 1", text = null, imageUri = null, createdAt = 200L)
        )

        every { dao.getNotesAsc(query) } returns flowOf(entities)

        val actualNotes = repository.getNotesAsc(query).first()

        assertEquals(expectedNotes, actualNotes)
    }

    @Test
    fun `deleteNoteById calls dao with correct id`() = runTest(testDispatcher) {
        val noteId = 99L

        coEvery { dao.deleteNoteById(noteId) } returns Unit

        repository.deleteNoteById(noteId)

        coVerify(exactly = 1) { dao.deleteNoteById(noteId) }
    }

    @Test
    fun `getNotesDesc returns empty list when database is empty`() = runTest(testDispatcher) {
        val query = "NonExistent"

        every { dao.getNotesDesc(query) } returns flowOf(emptyList())

        val actualNotes = repository.getNotesDesc(query).first()

        assertEquals(emptyList<Note>(), actualNotes)
    }
}