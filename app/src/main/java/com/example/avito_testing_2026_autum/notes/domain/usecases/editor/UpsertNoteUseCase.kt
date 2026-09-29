package com.example.avito_testing_2026_autum.notes.domain.usecases.editor

import com.example.avito_testing_2026_autum.notes.domain.model.Note
import com.example.avito_testing_2026_autum.notes.domain.repository.NoteRepository

class UpsertNoteUseCase(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(note: Note) {
        repository.upsertNote(note)
    }
}