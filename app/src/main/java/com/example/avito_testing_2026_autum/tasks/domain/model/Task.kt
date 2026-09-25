package com.example.avito_testing_2026_autum.tasks.domain.model

data class Task (
    val id: Long = 0L,
    val title: String,
    val isCompleted: Boolean = false,
)