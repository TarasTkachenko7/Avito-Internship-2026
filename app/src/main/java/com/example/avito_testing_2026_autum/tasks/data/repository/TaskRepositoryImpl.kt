package com.example.avito_testing_2026_autum.tasks.data.repository

import androidx.compose.runtime.disableHotReloadMode
import com.example.avito_testing_2026_autum.core.dispatchers.DispatchersProvider
import com.example.avito_testing_2026_autum.tasks.data.local.TaskDao
import com.example.avito_testing_2026_autum.tasks.data.mapper.toDomain
import com.example.avito_testing_2026_autum.tasks.data.mapper.toEntity
import com.example.avito_testing_2026_autum.tasks.domain.model.Task
import com.example.avito_testing_2026_autum.tasks.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.collections.map

class TaskRepositoryImpl(
    private val dao: TaskDao,
    private val dispatchers: DispatchersProvider
): TaskRepository {

    override fun getTasksDesc(query: String, isCompletedFilter: Boolean?): Flow<List<Task>> {
        return dao.getTasksDesc(query, isCompletedFilter)
            .map { list -> list.map { it.toDomain() } }
            .flowOn(dispatchers.io)
    }

    override fun getTasksAsc(query: String, isCompletedFilter: Boolean?): Flow<List<Task>> {
        return dao.getTasksAsc(query, isCompletedFilter)
            .map { list -> list.map { it.toDomain() } }
            .flowOn(dispatchers.io)
    }

    override suspend fun deleteTaskById(id: Long) = withContext(dispatchers.io) {
        dao.deleteTaskById(id)
    }

    override suspend fun upsertTask(task: Task) = withContext(dispatchers.io) {
        dao.upsertTask(task.toEntity())
    }

}