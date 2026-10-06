package com.example.avito_testing_2026_autum.notes.data.repository

import com.example.avito_testing_2026_autum.core.dispatchers.DispatchersProvider
import com.example.avito_testing_2026_autum.notes.data.local.NoteDao
import com.example.avito_testing_2026_autum.notes.data.mapper.toDomain
import com.example.avito_testing_2026_autum.notes.data.mapper.toEntity
import com.example.avito_testing_2026_autum.notes.domain.model.Note
import com.example.avito_testing_2026_autum.notes.domain.model.NoteSortOrder
import com.example.avito_testing_2026_autum.notes.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class NoteRepositoryImpl(
    private val dao: NoteDao,
    private val dispatchers: DispatchersProvider
) : NoteRepository {

    override fun getNotes(
        query: String,
        sortOrder: NoteSortOrder
    ): Flow<List<Note>> {
        val sanitizedQuery = query.trim()
        val entitiesFlow = when (sortOrder) {
            NoteSortOrder.DATE_DESC -> dao.getNotesDesc(sanitizedQuery)
            NoteSortOrder.DATE_ASC -> dao.getNotesAsc(sanitizedQuery)
        }

        return entitiesFlow
            .map { list -> list.map { it.toDomain() } }
            .flowOn(dispatchers.io)
    }

    override suspend fun getNoteById(id: Long): Note? {
        return dao.getNoteById(id)?.toDomain()
    }

    override suspend fun deleteNoteById(id: Long) {
        dao.deleteNoteById(id)
    }

    override suspend fun upsertNote(note: Note) {
        dao.upsertNote(note.toEntity())
    }

}