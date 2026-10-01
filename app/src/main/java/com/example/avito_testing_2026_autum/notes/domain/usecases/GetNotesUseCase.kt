package com.example.avito_testing_2026_autum.notes.domain.usecases

import com.example.avito_testing_2026_autum.notes.domain.model.Note
import com.example.avito_testing_2026_autum.notes.domain.model.NoteSortOrder
import com.example.avito_testing_2026_autum.notes.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow

class GetNotesUseCase(
    private val repository: NoteRepository
) {
    operator fun invoke(query: String, sortOrder: NoteSortOrder): Flow<List<Note>> {
        return repository.getNotes(query, sortOrder)
    }
}