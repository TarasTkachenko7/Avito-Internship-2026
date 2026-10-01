package com.example.avito_testing_2026_autum.tasks.domain.usecases

import com.example.avito_testing_2026_autum.tasks.domain.repository.TaskRepository

class ToggleTaskStatusUseCase(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(taskId: Long, isCompleted: Boolean) {
        repository.updateTaskStatus(taskId, isCompleted)
    }
}