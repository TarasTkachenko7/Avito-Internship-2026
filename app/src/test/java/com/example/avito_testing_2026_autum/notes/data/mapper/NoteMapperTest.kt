package com.example.avito_testing_2026_autum.notes.data.mapper

import com.example.avito_testing_2026_autum.notes.data.local.NoteEntity
import com.example.avito_testing_2026_autum.notes.domain.model.Note
import org.junit.Assert.assertEquals
import org.junit.Test

class NoteMapperTest {

    @Test
    fun `toDomain maps NoteEntity to Note correctly`() {
        val entity = NoteEntity(id = 1L, title = "Title", text = "Text", imageUri = "uri", createdAt = 100L)
        val expected = Note(id = 1L, title = "Title", text = "Text", imageUri = "uri", createdAt = 100L)

        assertEquals(expected, entity.toDomain())
    }

    @Test
    fun `toEntity maps Note to NoteEntity correctly`() {
        val domain = Note(id = 1L, title = "Title", text = "Text", imageUri = "uri", createdAt = 100L)
        val expected = NoteEntity(id = 1L, title = "Title", text = "Text", imageUri = "uri", createdAt = 100L)

        assertEquals(expected, domain.toEntity())
    }
}