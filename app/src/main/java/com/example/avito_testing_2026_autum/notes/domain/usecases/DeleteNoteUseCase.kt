package com.example.avito_testing_2026_autum.notes.domain.usecases

import com.example.avito_testing_2026_autum.notes.domain.repository.NoteRepository

class DeleteNoteUseCase(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(id: Long) {
        repository.deleteNoteById(id)
    }
}