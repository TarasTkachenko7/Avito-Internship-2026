package com.example.avito_testing_2026_autum.tasks.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("""
        SELECT * FROM tasks 
        WHERE title LIKE '%' || :query || '%' 
        AND (:isCompletedFilter IS NULL OR isCompleted = :isCompletedFilter) 
        ORDER BY isCompleted ASC, id DESC
    """)
    fun getTasksDesc(query: String = "", isCompletedFilter: Boolean? = null): Flow<List<TaskEntity>>

    @Query("""
        SELECT * FROM tasks 
        WHERE title LIKE '%' || :query || '%' 
        AND (:isCompletedFilter IS NULL OR isCompleted = :isCompletedFilter) 
        ORDER BY isCompleted ASC, id ASC
    """)
    fun getTasksAsc(query: String = "", isCompletedFilter: Boolean? = null): Flow<List<TaskEntity>>

    @Query("""
        DELETE FROM tasks 
        WHERE id = :taskId
        """)
    suspend fun deleteTaskById(taskId: Long)

    @Upsert
    suspend fun upsertTask(task: TaskEntity)

}