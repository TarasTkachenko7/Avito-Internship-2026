package com.example.avito_testing_2026_autum.notes.domain.usecases

import com.example.avito_testing_2026_autum.core.domain.manager.FileManager
import com.example.avito_testing_2026_autum.notes.domain.repository.NoteRepository

class DeleteNoteUseCase(
    private val repository: NoteRepository,
    private val fileManager: FileManager
) {
    suspend operator fun invoke(id: Long) {
        val note = repository.getNoteById(id)
        note?.imageUri?.let { uri ->
            fileManager.deleteFile(uri)
        }
        repository.deleteNoteById(id)
    }
}