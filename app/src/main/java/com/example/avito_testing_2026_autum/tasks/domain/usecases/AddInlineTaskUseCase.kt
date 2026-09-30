package com.example.avito_testing_2026_autum.tasks.domain.usecases

import com.example.avito_testing_2026_autum.tasks.domain.model.Task
import com.example.avito_testing_2026_autum.tasks.domain.repository.TaskRepository

class AddInlineTaskUseCase(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(rawTitle: String): Boolean {
        val cleanTitle = rawTitle.trim()
        if (cleanTitle.isBlank()) return false

        repository.upsertTask(Task(title = cleanTitle))
        return true
    }
}