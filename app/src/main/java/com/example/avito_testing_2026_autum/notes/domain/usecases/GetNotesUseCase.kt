package com.example.avito_testing_2026_autum.notes.domain.usecases

import com.example.avito_testing_2026_autum.notes.domain.model.Note
import com.example.avito_testing_2026_autum.notes.domain.repository.NoteRepository
import com.example.avito_testing_2026_autum.notes.presentation.contract.SortType
import kotlinx.coroutines.flow.Flow

class GetNotesUseCase(
    private val repository: NoteRepository
) {
    operator fun invoke(query: String, sortType: SortType): Flow<List<Note>> {
        return when (sortType) {
            SortType.DATE_DESC -> repository.getNotesDesc(query)
            SortType.DATE_ASC -> repository.getNotesAsc(query)
        }
    }
}