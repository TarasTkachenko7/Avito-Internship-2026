package com.example.avito_testing_2026_autum.tasks.presentation.models

import androidx.compose.runtime.Immutable

@Immutable
data class TaskUiModel(
    val id: Long,
    val title: String,
    val isCompleted: Boolean,
    val createdAt: String
)