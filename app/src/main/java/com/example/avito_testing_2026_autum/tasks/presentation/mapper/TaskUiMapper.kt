package com.example.avito_testing_2026_autum.tasks.presentation.mapper

import com.example.avito_testing_2026_autum.core.utils.toShortFormattedDateString
import com.example.avito_testing_2026_autum.tasks.domain.model.Task
import com.example.avito_testing_2026_autum.tasks.presentation.models.TaskUiModel

fun Task.toUiModel(): TaskUiModel {
    return TaskUiModel(
        id = id,
        title = title,
        isCompleted = isCompleted,
        createdAt = createdAt.toShortFormattedDateString()
    )
}