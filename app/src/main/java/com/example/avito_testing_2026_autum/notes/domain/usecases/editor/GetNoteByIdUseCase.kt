package com.example.avito_testing_2026_autum.notes.domain.usecases.editor

import com.example.avito_testing_2026_autum.notes.domain.model.Note
import com.example.avito_testing_2026_autum.notes.domain.repository.NoteRepository

class GetNoteByIdUseCase(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(noteId: Long): Note? {
        return repository.getNoteById(noteId)
    }
}