package com.example.avito_testing_2026_autum.notes.domain.usecases

import com.example.avito_testing_2026_autum.core.domain.manager.FileManager
import com.example.avito_testing_2026_autum.notes.domain.repository.NoteRepository
import kotlin.coroutines.cancellation.CancellationException

class DeleteNoteUseCase(
    private val repository: NoteRepository,
    private val fileManager: FileManager
) {
    suspend operator fun invoke(id: Long): Result<Unit> {
        return try {
            val note = repository.getNoteById(id) ?: return Result.success(Unit)

            repository.deleteNoteById(id)

            note.imageUri?.let { uri ->
                fileManager.deleteFile(uri)
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}