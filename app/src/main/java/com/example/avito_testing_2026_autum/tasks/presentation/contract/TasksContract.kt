package com.example.avito_testing_2026_autum.tasks.presentation.contract

import com.example.avito_testing_2026_autum.tasks.domain.model.TaskFilterType
import com.example.avito_testing_2026_autum.tasks.domain.model.TaskSortOrder
import com.example.avito_testing_2026_autum.tasks.presentation.models.TaskUiModel
import com.example.avito_testing_2026_autum.voice.domain.model.VoiceState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class TasksUiState(
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val appliedSearchQuery: String = "",
    val sortOrder: TaskSortOrder = TaskSortOrder.DATE_DESC,
    val filterType: TaskFilterType = TaskFilterType.ALL,
    val tasks: ImmutableList<TaskUiModel> = persistentListOf(),
    val newTaskTitle: String = "",
    val isCreatingTask: Boolean = false,
    val showCreateOptions: Boolean = false,
    val showVoiceDialog: Boolean = false,
    val voiceState: VoiceState = VoiceState.Idle,
    val isAiProcessing: Boolean = false
)

sealed interface TasksEvent {
    data class OnSearchQueryChanged(val query: String) : TasksEvent
    data object OnSearchClicked : TasksEvent
    data class OnSortClicked(val sortOrder: TaskSortOrder) : TasksEvent
    data class OnFilterClicked(val filterType: TaskFilterType) : TasksEvent
    data class OnTaskStatusChanged(val taskId: Long, val isCompleted: Boolean) : TasksEvent
    data class OnDeleteTaskClicked(val taskId: Long) : TasksEvent

    // События создания
    data object OnFabClicked : TasksEvent
    data object OnDismissCreateOptions : TasksEvent
    data object OnTextTaskClicked : TasksEvent

    // Голосовые события
    data object OnVoiceTaskClicked : TasksEvent
    data object OnStopVoiceListening : TasksEvent
    data object OnDismissVoiceDialog : TasksEvent
    data object OnPermissionDenied : TasksEvent

    data class OnNewTaskTitleChanged(val title: String) : TasksEvent
    data object OnSaveNewTask : TasksEvent
    data object OnCancelNewTask : TasksEvent
}

sealed interface TasksEffect {
    data object ScrollToTop : TasksEffect
    data object FocusOnNewTask : TasksEffect
    data class ShowError(val message: String) : TasksEffect
}