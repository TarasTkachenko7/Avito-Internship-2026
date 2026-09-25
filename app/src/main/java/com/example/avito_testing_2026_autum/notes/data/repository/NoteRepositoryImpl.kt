package com.example.avito_testing_2026_autum.notes.data.repository

import com.example.avito_testing_2026_autum.core.data.local.AppDatabase
import com.example.avito_testing_2026_autum.core.dispatchers.DispatchersProvider
import com.example.avito_testing_2026_autum.notes.data.local.NoteDao
import com.example.avito_testing_2026_autum.notes.data.mapper.toDomain
import com.example.avito_testing_2026_autum.notes.data.mapper.toEntity
import com.example.avito_testing_2026_autum.notes.domain.model.Note
import com.example.avito_testing_2026_autum.notes.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class NoteRepositoryImpl(
    private val dao: NoteDao,
    private val dispatchers: DispatchersProvider
): NoteRepository {

    override fun getNotesDesc(query: String): Flow<List<Note>> {
        return dao.getNotesDesc(query)
            .map { list -> list.map { it.toDomain() } }
            .flowOn(dispatchers.io)
    }

    override fun getNotesAsc(query: String): Flow<List<Note>> {
        return dao.getNotesAsc(query)
            .map { list -> list.map { it.toDomain() } }
            .flowOn(dispatchers.io)
    }

    override suspend fun getNoteById(id: Long): Note? = withContext(dispatchers.io) {
        return@withContext dao.getNoteById(id)?.toDomain()
    }

    override suspend fun deleteNoteById(id: Long) = withContext(dispatchers.io){
        dao.deleteNoteById(id)
    }

    override suspend fun upsertNote(note: Note) = withContext(dispatchers.io) {
        dao.upsertNote(note.toEntity())
    }

}