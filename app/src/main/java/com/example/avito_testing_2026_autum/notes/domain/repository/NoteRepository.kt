package com.example.avito_testing_2026_autum.notes.domain.repository

import com.example.avito_testing_2026_autum.notes.domain.model.Note
import com.example.avito_testing_2026_autum.notes.domain.model.NoteSortOrder
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun getNotes(
        query: String = "",
        sortOrder: NoteSortOrder = NoteSortOrder.DATE_DESC
    ): Flow<List<Note>>

    suspend fun getNoteById(id: Long): Note?
    suspend fun upsertNote(note: Note)
    suspend fun deleteNoteById(id: Long)
}