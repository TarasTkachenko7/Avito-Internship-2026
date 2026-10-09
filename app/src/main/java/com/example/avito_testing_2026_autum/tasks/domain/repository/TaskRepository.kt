package com.example.avito_testing_2026_autum.tasks.domain.repository

import com.example.avito_testing_2026_autum.tasks.domain.model.Task
import com.example.avito_testing_2026_autum.tasks.domain.model.TaskFilterType
import com.example.avito_testing_2026_autum.tasks.domain.model.TaskSortOrder
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun getTasks(
        query: String = "",
        sortOrder: TaskSortOrder = TaskSortOrder.DATE_DESC,
        filterType: TaskFilterType = TaskFilterType.ALL
    ): Flow<List<Task>>

    suspend fun upsertTask(task: Task)
    suspend fun updateTaskStatus(taskId: Long, isCompleted: Boolean)
    suspend fun updateTaskTitle(taskId: Long, title: String)
    suspend fun deleteTaskById(id: Long)
}