package com.example.avito_testing_2026_autum.tasks.data.mapper

import com.example.avito_testing_2026_autum.tasks.data.local.TaskEntity
import com.example.avito_testing_2026_autum.tasks.domain.model.Task
import org.junit.Assert.assertEquals
import org.junit.Test

class TaskMapperTest {

    @Test
    fun `toDomain maps TaskEntity to Task correctly`() {
        val entity = TaskEntity(id = 1L, title = "Task", isCompleted = true)
        val expected = Task(id = 1L, title = "Task", isCompleted = true)

        assertEquals(expected, entity.toDomain())
    }

    @Test
    fun `toEntity maps Task to TaskEntity correctly`() {
        val domain = Task(id = 1L, title = "Task", isCompleted = true)
        val expected = TaskEntity(id = 1L, title = "Task", isCompleted = true)

        assertEquals(expected, domain.toEntity())
    }
}