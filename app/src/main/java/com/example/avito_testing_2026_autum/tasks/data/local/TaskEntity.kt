package com.example.avito_testing_2026_autum.tasks.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tasks",
    indices = [
        Index(value = ["isCompleted", "createdAt"])
    ]
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val isCompleted: Boolean,
    val createdAt: Long
)