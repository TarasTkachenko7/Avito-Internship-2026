package com.example.avito_testing_2026_autum.notes.domain.repository

import com.example.avito_testing_2026_autum.notes.domain.model.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepository {

    fun getNotesDesc(query: String = ""): Flow<List<Note>>
    fun getNotesAsc(query: String = ""): Flow<List<Note>>
    suspend fun getNoteById(id: Long): Note?
    suspend fun upsertNote(note: Note)
    suspend fun deleteNoteById(id: Long)

}