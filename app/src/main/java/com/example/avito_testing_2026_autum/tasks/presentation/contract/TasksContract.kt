package com.example.avito_testing_2026_autum.tasks.presentation.contract

import com.example.avito_testing_2026_autum.tasks.domain.model.TaskFilterType
import com.example.avito_testing_2026_autum.tasks.domain.model.TaskSortOrder
import com.example.avito_testing_2026_autum.tasks.presentation.models.TaskUiModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class TasksUiState(
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val sortOrder: TaskSortOrder = TaskSortOrder.DATE_DESC,
    val filterType: TaskFilterType = TaskFilterType.ALL,
    val tasks: ImmutableList<TaskUiModel> = persistentListOf(),
    val newTaskTitle: String = "",
    val isCreatingTask: Boolean = false
)

sealed interface TasksEvent {
    data class OnSearchQueryChanged(val query: String) : TasksEvent
    data class OnSortClicked(val sortOrder: TaskSortOrder) : TasksEvent
    data class OnFilterClicked(val filterType: TaskFilterType) : TasksEvent
    data class OnTaskStatusChanged(val taskId: Long, val isCompleted: Boolean) : TasksEvent
    data class OnDeleteTaskClicked(val taskId: Long) : TasksEvent
    data object OnAddNewTaskClicked : TasksEvent
    data class OnNewTaskTitleChanged(val title: String) : TasksEvent
    data object OnSaveNewTask : TasksEvent
    data object OnCancelNewTask : TasksEvent
}

sealed interface TasksEffect {
    data object ScrollToTop : TasksEffect
    data object FocusOnNewTask : TasksEffect
    data class ShowError(val message: String) : TasksEffect
}