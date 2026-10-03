package com.example.avito_testing_2026_autum.tasks.domain.usecases

import com.example.avito_testing_2026_autum.tasks.domain.model.Task
import com.example.avito_testing_2026_autum.tasks.domain.model.TaskSortOrder
import com.example.avito_testing_2026_autum.tasks.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow

class GetTasksUseCase(
    private val repository: TaskRepository
) {
    operator fun invoke(
        query: String = "",
        sortOrder: TaskSortOrder = TaskSortOrder.DATE_DESC,
        isCompletedFilter: Boolean? = null
    ): Flow<List<Task>> {
        return repository.getTasks(query, sortOrder, isCompletedFilter)
    }
}