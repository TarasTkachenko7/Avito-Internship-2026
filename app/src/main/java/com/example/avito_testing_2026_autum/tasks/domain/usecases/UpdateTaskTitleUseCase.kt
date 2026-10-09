package com.example.avito_testing_2026_autum.tasks.domain.usecases

import com.example.avito_testing_2026_autum.tasks.domain.repository.TaskRepository
import kotlin.coroutines.cancellation.CancellationException

class UpdateTaskTitleUseCase(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(taskId: Long, rawTitle: String): Result<Unit> {
        val cleanTitle = rawTitle.trim()
        if (cleanTitle.isBlank()) {
            return Result.failure(IllegalArgumentException())
        }

        return try {
            repository.updateTaskTitle(taskId, cleanTitle)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }
}