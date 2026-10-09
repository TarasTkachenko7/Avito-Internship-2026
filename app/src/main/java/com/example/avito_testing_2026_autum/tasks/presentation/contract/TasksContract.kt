package com.example.avito_testing_2026_autum.tasks.presentation.contract

import androidx.compose.runtime.Immutable
import com.example.avito_testing_2026_autum.core.utils.UiText
import com.example.avito_testing_2026_autum.tasks.domain.model.TaskFilterType
import com.example.avito_testing_2026_autum.tasks.domain.model.TaskSortOrder
import com.example.avito_testing_2026_autum.tasks.presentation.models.TaskUiModel
import com.example.avito_testing_2026_autum.voice.domain.model.VoiceState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
sealed interface TasksModalOverlay {
    data object CreateOptions : TasksModalOverlay
    data class VoiceInput(val voiceState: VoiceState = VoiceState.Idle) : TasksModalOverlay
    data object AiProcessing : TasksModalOverlay
}

@Immutable
data class TasksUiState(
    val isLoading: Boolean = true,
    val tasks: ImmutableList<TaskUiModel> = persistentListOf(),

    val appliedSearchQuery: String = "",
    val sortOrder: TaskSortOrder = TaskSortOrder.DATE_DESC,
    val filterType: TaskFilterType = TaskFilterType.ALL,

    val isCreatingTask: Boolean = false,
    val editingTaskId: Long? = null,
    val isDeleteModeActive: Boolean = false,

    val currentOverlay: TasksModalOverlay? = null
)

sealed interface TasksEvent {
    // 1. Поиск, сортировка и фильтрация
    sealed interface Query : TasksEvent {
        data class SearchSubmit(val query: String) : Query
        data object SearchClear : Query
        data class SortSelected(val sortOrder: TaskSortOrder) : Query
        data class FilterSelected(val filterType: TaskFilterType) : Query
    }

    // 2. Взаимодействие со списком
    sealed interface Action : TasksEvent {
        data class StatusChanged(val taskId: Long, val isCompleted: Boolean) : Action
        data class DeleteClicked(val taskId: Long) : Action
        data object ToggleDeleteMode : Action
    }

    // 3. Инлайн-создание задачи текстом
    sealed interface Creation : TasksEvent {
        data object Start : Creation
        data class Save(val title: String) : Creation
        data object Cancel : Creation
    }

    // 4. Редактирование задачи
    sealed interface Edit : TasksEvent {
        data class Start(val taskId: Long) : Edit
        data class Save(val taskId: Long, val newTitle: String) : Edit
        data object Cancel : Edit
    }

    // 5. Модальные окна и голосовой ввод
    sealed interface Modal : TasksEvent {
        data object FabClicked : Modal
        data object DismissCurrentModal : Modal
        data object StartVoice : Modal
        data object VoiceConfirm : Modal
        data object PermissionDenied : Modal
    }
}

sealed interface TasksEffect {
    data class ShowError(val message: UiText) : TasksEffect
    data object RequestRecordAudioPermission : TasksEffect
    data object FocusOnNewTask : TasksEffect
    data object ScrollToTop : TasksEffect
}