package com.example.avito_testing_2026_autum.tasks.domain.repository

import com.example.avito_testing_2026_autum.tasks.domain.model.Task
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun getTasksDesc(query: String = "", isCompletedFilter: Boolean? = null): Flow<List<Task>>
    fun getTasksAsc(query: String = "", isCompletedFilter: Boolean? = null): Flow<List<Task>>
    suspend fun upsertTask(task: Task)
    suspend fun deleteTaskById(id: Long)
}