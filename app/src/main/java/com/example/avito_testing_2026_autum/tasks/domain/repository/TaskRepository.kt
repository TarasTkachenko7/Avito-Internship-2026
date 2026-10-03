package com.example.avito_testing_2026_autum.tasks.domain.repository

import com.example.avito_testing_2026_autum.tasks.domain.model.Task
import com.example.avito_testing_2026_autum.tasks.domain.model.TaskSortOrder
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun getTasks(
        query: String = "",
        sortOrder: TaskSortOrder,
        isCompletedFilter: Boolean? = null
    ): Flow<List<Task>>

    suspend fun upsertTask(task: Task)
    suspend fun updateTaskStatus(taskId: Long, isCompleted: Boolean)
    suspend fun deleteTaskById(id: Long)
}