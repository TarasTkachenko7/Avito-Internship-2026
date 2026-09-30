package com.example.avito_testing_2026_autum.tasks.domain.usecases

import com.example.avito_testing_2026_autum.tasks.domain.model.Task
import com.example.avito_testing_2026_autum.tasks.domain.repository.TaskRepository
import com.example.avito_testing_2026_autum.tasks.presentation.contract.SortType
import kotlinx.coroutines.flow.Flow

class GetTasksUseCase(
    private val repository: TaskRepository
) {
    operator fun invoke(
        query: String = "",
        sortType: SortType = SortType.DATE_DESC,
        isCompletedFilter: Boolean? = null
    ): Flow<List<Task>> {
        return when (sortType) {
            SortType.DATE_DESC -> repository.getTasksDesc(query, isCompletedFilter)
            SortType.DATE_ASC -> repository.getTasksAsc(query, isCompletedFilter)
        }
    }
}