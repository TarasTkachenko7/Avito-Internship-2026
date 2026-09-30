package com.example.avito_testing_2026_autum.tasks.presentation.contract

import com.example.avito_testing_2026_autum.tasks.domain.model.Task

enum class SortType {
    DATE_DESC,
    DATE_ASC
}

data class TasksUiState(
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val sortType: SortType = SortType.DATE_DESC,
    val tasks: List<Task> = emptyList(),
    val taskIdToDelete: Long? = null,
    val newTaskTitle: String = "",
    val isCreatingTask: Boolean = false,
)

sealed interface TasksEvent {
    data class OnSearchQueryChanged(val query: String) : TasksEvent
    data class OnSortClicked(val sortType: SortType) : TasksEvent
    data class OnCheckBoxClicked(val task: Task, val isCompleted: Boolean) : TasksEvent
    data class OnDeleteTaskClicked(val taskId: Long) : TasksEvent
    data object OnAddNewTaskClicked : TasksEvent
    data class OnNewTaskTitleChanged(val title: String) : TasksEvent
    data object OnSaveNewTask : TasksEvent
    data object OnCancelNewTask : TasksEvent
}

sealed interface TasksEffect {
    data object FocusOnNewTask : TasksEffect
}