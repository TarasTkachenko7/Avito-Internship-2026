package com.example.avito_testing_2026_autum.tasks.domain.usecases

import com.example.avito_testing_2026_autum.tasks.domain.model.Task
import com.example.avito_testing_2026_autum.tasks.domain.repository.TaskRepository

class ToggleTaskStatusUseCase(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(task: Task, isCompleted: Boolean) {
        if (task.isCompleted == isCompleted) return

        repository.upsertTask(task.copy(isCompleted = isCompleted))
    }
}