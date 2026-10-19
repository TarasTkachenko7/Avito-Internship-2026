package com.example.avito_testing_2026_autum.tasks.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.avito_testing_2026_autum.R
import com.example.avito_testing_2026_autum.ai.domain.usecases.FormulateTaskUseCase
import com.example.avito_testing_2026_autum.core.utils.UiText
import com.example.avito_testing_2026_autum.tasks.domain.model.TaskFilterType
import com.example.avito_testing_2026_autum.tasks.domain.usecases.DeleteTaskByIdUseCase
import com.example.avito_testing_2026_autum.tasks.domain.usecases.GetTasksUseCase
import com.example.avito_testing_2026_autum.tasks.domain.usecases.ToggleTaskStatusUseCase
import com.example.avito_testing_2026_autum.tasks.domain.usecases.UpdateTaskTitleUseCase
import com.example.avito_testing_2026_autum.tasks.domain.usecases.UpsertTaskUseCase
import com.example.avito_testing_2026_autum.tasks.presentation.contract.TasksEffect
import com.example.avito_testing_2026_autum.tasks.presentation.contract.TasksEvent
import com.example.avito_testing_2026_autum.tasks.presentation.contract.TasksModalOverlay
import com.example.avito_testing_2026_autum.tasks.presentation.contract.TasksUiState
import com.example.avito_testing_2026_autum.tasks.presentation.mapper.toUiModel
import com.example.avito_testing_2026_autum.tasks.presentation.models.TaskUiModel
import com.example.avito_testing_2026_autum.voice.domain.model.VoiceState
import com.example.avito_testing_2026_autum.voice.domain.recognition.SpeechRecognizerContract
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException

@OptIn(ExperimentalCoroutinesApi::class)
class TasksViewModel(
    private val getTasksUseCase: GetTasksUseCase,
    private val upsertTaskUseCase: UpsertTaskUseCase,
    private val toggleTaskStatusUseCase: ToggleTaskStatusUseCase,
    private val updateTaskTitleUseCase: UpdateTaskTitleUseCase,
    private val deleteTaskUseCase: DeleteTaskByIdUseCase,
    private val formulateTaskUseCase: FormulateTaskUseCase,
    private val speechRecognizer: SpeechRecognizerContract,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val retryTasksTrigger = MutableStateFlow(0)
    private val _state = MutableStateFlow(
        TasksUiState(
            isCreatingTask = savedStateHandle[KEY_IS_CREATING] ?: false,
            creationDraft = savedStateHandle[KEY_CREATION_DRAFT] ?: "",
            editingTaskId = savedStateHandle[KEY_EDITING_ID],
            editingDraftTitle = savedStateHandle[KEY_EDITING_DRAFT] ?: ""
        )
    )
    val state: StateFlow<TasksUiState> = _state.asStateFlow()

    private val _effect = Channel<TasksEffect>(capacity = Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        observeTasks()
        observeVoiceState()
    }

    private fun observeTasks() {
        viewModelScope.launch {
            combine(
                _state.map { Triple(it.appliedSearchQuery, it.sortOrder, it.filterType) }
                    .distinctUntilChanged(),
                retryTasksTrigger
            ) { parameters, _ -> parameters }
                .flatMapLatest { (query, sortType, filterType) ->
                    getTasksUseCase(query, sortType, filterType)
                        .map { domainTasks ->
                            withContext(Dispatchers.Default) {
                                domainTasks.map { it.toUiModel() }.toPersistentList() as ImmutableList<TaskUiModel>?
                            }
                        }
                        .catch {
                            _state.update { it.copy(isLoading = false, hasLoadError = true) }
                            sendEffect(TasksEffect.ShowError(UiText.StringResource(R.string.error_tasks_load)))
                            emit(null)
                        }
                }
                .collect { tasksList ->
                    if (tasksList != null) {
                        _state.update { it.copy(tasks = tasksList, isLoading = false, hasLoadError = false) }
                    }
                }
        }
    }

    private fun observeVoiceState() {
        speechRecognizer.voiceState
            .onEach { voiceState ->
                when (voiceState) {
                    is VoiceState.Success -> {
                        _state.update { it.copy(currentOverlay = TasksModalOverlay.AiProcessing) }
                        speechRecognizer.reset()
                        processVoiceWithGigaChat(voiceState.text)
                    }

                    else -> {
                        if (_state.value.currentOverlay is TasksModalOverlay.VoiceInput) {
                            _state.update {
                                it.copy(currentOverlay = TasksModalOverlay.VoiceInput(voiceState))
                            }
                        }
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun processVoiceWithGigaChat(rawText: String) {
        viewModelScope.launch {
            formulateTaskUseCase(rawText)
                .onSuccess { formulatedText ->
                    upsertTaskUseCase(rawTitle = formulatedText)
                    _state.update { it.copy(currentOverlay = null) }
                }
                .onFailure { error ->
                    _state.update { it.copy(currentOverlay = null) }
                    sendEffect(
                        TasksEffect.ShowError(UiText.StringResource(R.string.error_gigachat))
                    )
                }
        }
    }

    fun handleEvent(event: TasksEvent) {
        when (event) {
            is TasksEvent.Query -> handleQuery(event)
            is TasksEvent.Action -> handleAction(event)
            is TasksEvent.Creation -> handleCreation(event)
            is TasksEvent.Edit -> handleEdit(event)
            is TasksEvent.Modal -> handleModal(event)
        }
    }

    private fun handleQuery(event: TasksEvent.Query) {
        when (event) {
            is TasksEvent.Query.SearchSubmit -> updateTaskQuery { it.copy(appliedSearchQuery = event.query.trim()) }
            is TasksEvent.Query.SearchClear -> updateTaskQuery { it.copy(appliedSearchQuery = "") }
            is TasksEvent.Query.SortSelected -> {
                updateTaskQuery { it.copy(sortOrder = event.sortOrder) }
                sendEffect(TasksEffect.ScrollToTop)
            }
            is TasksEvent.Query.FilterSelected -> {
                updateTaskQuery { it.copy(filterType = event.filterType) }
                sendEffect(TasksEffect.ScrollToTop)
            }
            TasksEvent.Query.Retry -> {
                _state.update { it.copy(isLoading = true, hasLoadError = false) }
                retryTasksTrigger.update { it + 1 }
            }
        }
    }

    private fun updateTaskQuery(transform: (TasksUiState) -> TasksUiState) {
        _state.update { current ->
            val updated = transform(current)
            if (updated.appliedSearchQuery == current.appliedSearchQuery &&
                updated.sortOrder == current.sortOrder &&
                updated.filterType == current.filterType
            ) {
                updated
            } else {
                updated.copy(tasks = persistentListOf(), isLoading = true, hasLoadError = false)
            }
        }
    }

    private fun handleAction(event: TasksEvent.Action) {
        when (event) {
            is TasksEvent.Action.StatusChanged -> viewModelScope.launch {
                runTaskAction(R.string.error_task_update) {
                    toggleTaskStatusUseCase(event.taskId, event.isCompleted)
                }
            }

            is TasksEvent.Action.DeleteClicked -> viewModelScope.launch {
                runTaskAction(R.string.error_task_delete) { deleteTaskUseCase(event.taskId) }
            }

            is TasksEvent.Action.ToggleDeleteMode -> {
                _state.update { current ->
                    val nextMode = !current.isDeleteModeActive
                    current.copy(
                        isDeleteModeActive = nextMode,
                        isCreatingTask = false,
                        creationDraft = "",
                        editingTaskId = null,
                        editingDraftTitle = "",
                        currentOverlay = null
                    )
                }
                savedStateHandle[KEY_IS_CREATING] = false
                savedStateHandle[KEY_CREATION_DRAFT] = ""
                savedStateHandle.remove<Long>(KEY_EDITING_ID)
                savedStateHandle[KEY_EDITING_DRAFT] = ""
            }
        }
    }

    private fun handleCreation(event: TasksEvent.Creation) {
        when (event) {
            is TasksEvent.Creation.Start -> {
                updateTaskQuery {
                    it.copy(
                        isCreatingTask = true,
                        creationDraft = "",
                        currentOverlay = null,
                        isSavingCreation = false,
                        editingTaskId = null,
                        editingDraftTitle = "",
                        isSavingEdit = false,
                        isDeleteModeActive = false,
                        filterType = TaskFilterType.ALL
                    )
                }
                savedStateHandle[KEY_IS_CREATING] = true
                savedStateHandle[KEY_CREATION_DRAFT] = ""
                savedStateHandle.remove<Long>(KEY_EDITING_ID)
                savedStateHandle[KEY_EDITING_DRAFT] = ""
                sendEffect(TasksEffect.ScrollToTop)
                sendEffect(TasksEffect.FocusOnNewTask)
            }

            is TasksEvent.Creation.Cancel -> {
                _state.update { it.copy(isCreatingTask = false, creationDraft = "", isSavingCreation = false) }
                savedStateHandle[KEY_IS_CREATING] = false
                savedStateHandle[KEY_CREATION_DRAFT] = ""
            }

            is TasksEvent.Creation.DraftChanged -> {
                _state.update { it.copy(creationDraft = event.title) }
                savedStateHandle[KEY_CREATION_DRAFT] = event.title
            }

            is TasksEvent.Creation.Save -> {
                if (_state.value.isSavingCreation || !_state.value.isCreatingTask) return
                _state.update { it.copy(creationDraft = event.title, isSavingCreation = true) }
                savedStateHandle[KEY_CREATION_DRAFT] = event.title
                viewModelScope.launch {
                    upsertTaskUseCase(rawTitle = event.title)
                        .onSuccess {
                            _state.update { it.copy(isCreatingTask = false, creationDraft = "", isSavingCreation = false) }
                            savedStateHandle[KEY_IS_CREATING] = false
                            savedStateHandle[KEY_CREATION_DRAFT] = ""
                        }
                        .onFailure { error ->
                            _state.update { it.copy(isSavingCreation = false) }
                            if (error is IllegalArgumentException) {
                                sendEffect(TasksEffect.ShowError(UiText.StringResource(R.string.error_empty_title)))
                            } else {
                                sendEffect(TasksEffect.ShowError(UiText.StringResource(R.string.error_task_save)))
                            }
                        }
                }
            }
        }
    }

    private fun handleEdit(event: TasksEvent.Edit) {
        when (event) {
            is TasksEvent.Edit.Start -> {
                val title = _state.value.tasks.firstOrNull { it.id == event.taskId }?.title.orEmpty()
                _state.update {
                    it.copy(
                        editingTaskId = event.taskId,
                        editingDraftTitle = title,
                        isSavingEdit = false,
                        isDeleteModeActive = false,
                        isCreatingTask = false,
                        currentOverlay = null
                    )
                }
                savedStateHandle[KEY_EDITING_ID] = event.taskId
                savedStateHandle[KEY_EDITING_DRAFT] = title
                savedStateHandle[KEY_IS_CREATING] = false
                savedStateHandle[KEY_CREATION_DRAFT] = ""
            }

            is TasksEvent.Edit.Cancel -> {
                _state.update { it.copy(editingTaskId = null, editingDraftTitle = "", isSavingEdit = false) }
                savedStateHandle.remove<Long>(KEY_EDITING_ID)
                savedStateHandle[KEY_EDITING_DRAFT] = ""
            }

            is TasksEvent.Edit.DraftChanged -> {
                if (_state.value.editingTaskId != event.taskId) return
                _state.update { it.copy(editingDraftTitle = event.title) }
                savedStateHandle[KEY_EDITING_DRAFT] = event.title
            }

            is TasksEvent.Edit.Save -> {
                if (_state.value.editingTaskId != event.taskId || _state.value.isSavingEdit) return
                _state.update { it.copy(editingDraftTitle = event.newTitle, isSavingEdit = true) }
                savedStateHandle[KEY_EDITING_DRAFT] = event.newTitle
                viewModelScope.launch {
                    updateTaskTitleUseCase(event.taskId, event.newTitle)
                        .onSuccess {
                            _state.update { it.copy(editingTaskId = null, editingDraftTitle = "", isSavingEdit = false) }
                            savedStateHandle.remove<Long>(KEY_EDITING_ID)
                            savedStateHandle[KEY_EDITING_DRAFT] = ""
                        }
                        .onFailure { error ->
                            _state.update { it.copy(isSavingEdit = false) }
                            if (error is IllegalArgumentException) {
                                sendEffect(TasksEffect.ShowError(UiText.StringResource(R.string.error_empty_title)))
                            } else {
                                sendEffect(TasksEffect.ShowError(UiText.StringResource(R.string.error_task_update)))
                            }
                        }
                }
            }
        }
    }

    private fun handleModal(event: TasksEvent.Modal) {
        when (event) {
            is TasksEvent.Modal.FabClicked -> {
                _state.update {
                    it.copy(
                        currentOverlay = TasksModalOverlay.CreateOptions,
                        isCreatingTask = false,
                        creationDraft = "",
                        editingTaskId = null,
                        editingDraftTitle = "",
                        isDeleteModeActive = false
                    )
                }
                savedStateHandle[KEY_IS_CREATING] = false
                savedStateHandle[KEY_CREATION_DRAFT] = ""
                savedStateHandle.remove<Long>(KEY_EDITING_ID)
                savedStateHandle[KEY_EDITING_DRAFT] = ""
            }

            is TasksEvent.Modal.DismissCurrentModal -> {
                speechRecognizer.cancel()
                _state.update { it.copy(currentOverlay = null) }
            }

            is TasksEvent.Modal.StartVoice -> {
                _state.update { it.copy(currentOverlay = TasksModalOverlay.VoiceInput()) }
                speechRecognizer.startListening()
            }

            is TasksEvent.Modal.VoiceConfirm -> speechRecognizer.stopListening()
            is TasksEvent.Modal.PermissionDenied -> {
                _state.update { it.copy(currentOverlay = null) }
                sendEffect(
                    TasksEffect.ShowError(UiText.StringResource(R.string.voice_error_permission_denied))
                )
            }
        }
    }

    private fun sendEffect(effect: TasksEffect) {
        _effect.trySend(effect)
    }

    private suspend fun runTaskAction(errorMessageId: Int, action: suspend () -> Unit) {
        try {
            action()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            sendEffect(TasksEffect.ShowError(UiText.StringResource(errorMessageId)))
        }
    }

    override fun onCleared() {
        super.onCleared()
        speechRecognizer.cancel()
    }

    private companion object {
        const val KEY_IS_CREATING = "tasks.isCreating"
        const val KEY_CREATION_DRAFT = "tasks.creationDraft"
        const val KEY_EDITING_ID = "tasks.editingId"
        const val KEY_EDITING_DRAFT = "tasks.editingDraft"
    }

}
