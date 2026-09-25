package com.example.avito_testing_2026_autum.tasks.data.mapper

import com.example.avito_testing_2026_autum.tasks.data.local.TaskEntity
import com.example.avito_testing_2026_autum.tasks.domain.model.Task

fun TaskEntity.toDomain(): Task {
    return Task(
        id = id,
        title = title,
        isCompleted = isCompleted
    )
}

fun Task.toEntity(): TaskEntity {
    return TaskEntity(
        id = id,
        title = title,
        isCompleted = isCompleted
    )
}