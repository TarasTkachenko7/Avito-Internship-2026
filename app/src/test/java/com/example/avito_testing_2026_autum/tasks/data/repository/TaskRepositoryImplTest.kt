package com.example.avito_testing_2026_autum.tasks.data.repository

import com.example.avito_testing_2026_autum.core.dispatchers.TestDispatchersProvider
import com.example.avito_testing_2026_autum.tasks.data.local.TaskDao
import com.example.avito_testing_2026_autum.tasks.data.local.TaskEntity
import com.example.avito_testing_2026_autum.tasks.domain.model.Task
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class TaskRepositoryImplTest {

    private lateinit var dao: TaskDao
    private lateinit var repository: TaskRepositoryImpl
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        dao = mockk()
        val dispatchersProvider = TestDispatchersProvider(testDispatcher)
        repository = TaskRepositoryImpl(dao, dispatchersProvider)
    }

    @Test
    fun `getTasksDesc returns flow of mapped tasks and passes null filter`() = runTest(testDispatcher) {
        val query = "Buy"
        val filter: Boolean? = null
        val entities = listOf(
            TaskEntity(id = 2L, title = "Buy milk", isCompleted = false),
            TaskEntity(id = 1L, title = "Buy bread", isCompleted = true)
        )
        val expectedTasks = listOf(
            Task(id = 2L, title = "Buy milk", isCompleted = false),
            Task(id = 1L, title = "Buy bread", isCompleted = true)
        )

        every { dao.getTasksDesc(query, filter) } returns flowOf(entities)

        val actualTasks = repository.getTasksDesc(query, filter).first()

        assertEquals(expectedTasks, actualTasks)

        coVerify(exactly = 1) { dao.getTasksDesc(query, filter) }
    }

    @Test
    fun `getTasksAsc returns flow of mapped tasks and passes specific filter`() = runTest(testDispatcher) {
        val query = ""
        val filter = false
        val entities = listOf(
            TaskEntity(id = 1L, title = "Task 1", isCompleted = false),
            TaskEntity(id = 2L, title = "Task 2", isCompleted = false)
        )
        val expectedTasks = listOf(
            Task(id = 1L, title = "Task 1", isCompleted = false),
            Task(id = 2L, title = "Task 2", isCompleted = false)
        )

        every { dao.getTasksAsc(query, filter) } returns flowOf(entities)

        val actualTasks = repository.getTasksAsc(query, filter).first()

        assertEquals(expectedTasks, actualTasks)
        coVerify(exactly = 1) { dao.getTasksAsc(query, filter) }
    }

    @Test
    fun `getTasksDesc returns empty list when dao is empty`() = runTest(testDispatcher) {
        val query = "NonExistent"

        every { dao.getTasksDesc(query, null) } returns flowOf(emptyList())

        val actualTasks = repository.getTasksDesc(query, null).first()

        assertEquals(emptyList<Task>(), actualTasks)
    }

    @Test
    fun `upsertTask maps domain to entity and calls dao`() = runTest(testDispatcher) {
        val task = Task(id = 1L, title = "New Task", isCompleted = true)
        val expectedEntity = TaskEntity(id = 1L, title = "New Task", isCompleted = true)

        coEvery { dao.upsertTask(any()) } returns Unit

        repository.upsertTask(task)

        coVerify(exactly = 1) { dao.upsertTask(expectedEntity) }
    }

    @Test
    fun `deleteTaskById calls dao with correct id`() = runTest(testDispatcher) {
        val taskId = 42L

        coEvery { dao.deleteTaskById(taskId) } returns Unit

        repository.deleteTaskById(taskId)

        coVerify(exactly = 1) { dao.deleteTaskById(taskId) }
    }
}