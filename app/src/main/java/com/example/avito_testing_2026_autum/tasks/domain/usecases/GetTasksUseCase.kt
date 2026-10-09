package com.example.avito_testing_2026_autum.tasks.domain.usecases

import com.example.avito_testing_2026_autum.tasks.domain.model.Task
import com.example.avito_testing_2026_autum.tasks.domain.model.TaskFilterType
import com.example.avito_testing_2026_autum.tasks.domain.model.TaskSortOrder
import com.example.avito_testing_2026_autum.tasks.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow

class GetTasksUseCase(
    private val repository: TaskRepository
) {
    operator fun invoke(
        query: String = "",
        sortOrder: TaskSortOrder = TaskSortOrder.DATE_DESC,
        filterType: TaskFilterType = TaskFilterType.ALL
    ): Flow<List<Task>> {
        val sanitizedQuery = query.trim()
        return repository.getTasks(sanitizedQuery, sortOrder, filterType)
    }
}