package com.example.avito_testing_2026_autum.tasks.data.repository

import com.example.avito_testing_2026_autum.core.dispatchers.DispatchersProvider
import com.example.avito_testing_2026_autum.tasks.data.local.TaskDao
import com.example.avito_testing_2026_autum.tasks.data.mapper.toDomain
import com.example.avito_testing_2026_autum.tasks.data.mapper.toEntity
import com.example.avito_testing_2026_autum.tasks.domain.model.Task
import com.example.avito_testing_2026_autum.tasks.domain.model.TaskSortOrder
import com.example.avito_testing_2026_autum.tasks.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class TaskRepositoryImpl(
    private val dao: TaskDao,
    private val dispatchers: DispatchersProvider
) : TaskRepository {

    override fun getTasks(
        query: String,
        sortOrder: TaskSortOrder,
        isCompletedFilter: Boolean?
    ): Flow<List<Task>> {
        val entitiesFlow = when (sortOrder) {
            TaskSortOrder.DATE_DESC -> dao.getTasksDesc(query, isCompletedFilter)
            TaskSortOrder.DATE_ASC -> dao.getTasksAsc(query, isCompletedFilter)
        }
        return entitiesFlow
            .map { list -> list.map { it.toDomain() } }
            .flowOn(dispatchers.io)
    }

    override suspend fun deleteTaskById(id: Long) = withContext(dispatchers.io) {
        dao.deleteTaskById(id)
    }

    override suspend fun upsertTask(task: Task) = withContext(dispatchers.io) {
        dao.upsertTask(task.toEntity())
    }

    override suspend fun updateTaskStatus(taskId: Long, isCompleted: Boolean) = withContext(dispatchers.io) {
        dao.updateTaskStatus(taskId, isCompleted)
    }

}