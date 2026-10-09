package com.example.avito_testing_2026_autum.tasks.domain.usecases

import com.example.avito_testing_2026_autum.tasks.domain.model.Task
import com.example.avito_testing_2026_autum.tasks.domain.repository.TaskRepository
import kotlin.coroutines.cancellation.CancellationException

class UpsertTaskUseCase(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(
        id: Long = 0L,
        rawTitle: String,
        isCompleted: Boolean = false,
        createdAt: Long? = null
    ): Result<Unit> {
        val cleanTitle = rawTitle.trim()

        if (cleanTitle.isBlank()) {
            return Result.failure(IllegalArgumentException())
        }

        val task = Task(
            id = id,
            title = cleanTitle,
            isCompleted = isCompleted,
            createdAt = createdAt ?: System.currentTimeMillis()
        )

        return try {
            repository.upsertTask(task)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }
}