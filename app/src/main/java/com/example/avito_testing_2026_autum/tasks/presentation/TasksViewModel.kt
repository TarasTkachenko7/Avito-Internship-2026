package com.example.avito_testing_2026_autum.tasks.presentation

import androidx.lifecycle.ViewModel
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
import com.example.avito_testing_2026_autum.voice.domain.model.VoiceState
import com.example.avito_testing_2026_autum.voice.domain.recognition.SpeechRecognizerContract
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
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

@OptIn(ExperimentalCoroutinesApi::class)
class TasksViewModel(
    private val getTasksUseCase: GetTasksUseCase,
    private val upsertTaskUseCase: UpsertTaskUseCase,
    private val toggleTaskStatusUseCase: ToggleTaskStatusUseCase,
    private val updateTaskTitleUseCase: UpdateTaskTitleUseCase,
    private val deleteTaskUseCase: DeleteTaskByIdUseCase,
    private val formulateTaskUseCase: FormulateTaskUseCase,
    private val speechRecognizer: SpeechRecognizerContract
) : ViewModel() {

    private val _state = MutableStateFlow(TasksUiState())
    val state: StateFlow<TasksUiState> = _state.asStateFlow()

    private val _effect = Channel<TasksEffect>(capacity = Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        observeTasks()
        observeVoiceState()
    }

    private fun observeTasks() {
        viewModelScope.launch {
            _state
                .map { Triple(it.appliedSearchQuery, it.sortOrder, it.filterType) }
                .distinctUntilChanged()
                .flatMapLatest { (query, sortType, filterType) ->
                    getTasksUseCase(query, sortType, filterType)
                        .map { domainTasks ->
                            domainTasks.map { it.toUiModel() }.toPersistentList()
                        }
                        .catch { emit(persistentListOf()) }
                }
                .collect { tasksList ->
                    _state.update { it.copy(tasks = tasksList, isLoading = false) }
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
            is TasksEvent.Query.SearchSubmit -> _state.update { it.copy(appliedSearchQuery = event.query.trim()) }
            is TasksEvent.Query.SearchClear -> _state.update { it.copy(appliedSearchQuery = "") }
            is TasksEvent.Query.SortSelected -> {
                _state.update { it.copy(sortOrder = event.sortOrder) }
                sendEffect(TasksEffect.ScrollToTop)
            }
            is TasksEvent.Query.FilterSelected -> {
                _state.update { it.copy(filterType = event.filterType) }
                sendEffect(TasksEffect.ScrollToTop)
            }
        }
    }

    private fun handleAction(event: TasksEvent.Action) {
        when (event) {
            is TasksEvent.Action.StatusChanged -> viewModelScope.launch {
                toggleTaskStatusUseCase(event.taskId, event.isCompleted)
            }

            is TasksEvent.Action.DeleteClicked -> viewModelScope.launch {
                deleteTaskUseCase(event.taskId)
            }

            is TasksEvent.Action.ToggleDeleteMode -> {
                _state.update { current ->
                    val nextMode = !current.isDeleteModeActive
                    current.copy(
                        isDeleteModeActive = nextMode,
                        isCreatingTask = false,
                        editingTaskId = null,
                        currentOverlay = null
                    )
                }
            }
        }
    }

    private fun handleCreation(event: TasksEvent.Creation) {
        when (event) {
            is TasksEvent.Creation.Start -> {
                _state.update {
                    it.copy(
                        isCreatingTask = true,
                        currentOverlay = null,
                        editingTaskId = null,
                        isDeleteModeActive = false,
                        filterType = TaskFilterType.ALL
                    )
                }
                sendEffect(TasksEffect.ScrollToTop)
                sendEffect(TasksEffect.FocusOnNewTask)
            }

            is TasksEvent.Creation.Cancel -> {
                _state.update { it.copy(isCreatingTask = false) }
            }

            is TasksEvent.Creation.Save -> {
                viewModelScope.launch {
                    upsertTaskUseCase(rawTitle = event.title)
                        .onSuccess {
                            _state.update { it.copy(isCreatingTask = false) }
                        }
                        .onFailure { error ->
                            if (error is IllegalArgumentException) {
                                sendEffect(TasksEffect.ShowError(UiText.StringResource(R.string.error_empty_title)))
                            }
                        }
                }
            }
        }
    }

    private fun handleEdit(event: TasksEvent.Edit) {
        when (event) {
            is TasksEvent.Edit.Start -> {
                _state.update {
                    it.copy(
                        editingTaskId = event.taskId,
                        isDeleteModeActive = false,
                        isCreatingTask = false,
                        currentOverlay = null
                    )
                }
            }

            is TasksEvent.Edit.Cancel -> {
                _state.update { it.copy(editingTaskId = null) }
            }

            is TasksEvent.Edit.Save -> {
                viewModelScope.launch {
                    updateTaskTitleUseCase(event.taskId, event.newTitle)
                        .onSuccess {
                            _state.update { it.copy(editingTaskId = null) }
                        }
                        .onFailure { error ->
                            if (error is IllegalArgumentException) {
                                sendEffect(TasksEffect.ShowError(UiText.StringResource(R.string.error_empty_title)))
                            }
                        }
                }
            }
        }
    }

    private fun handleModal(event: TasksEvent.Modal) {
        when (event) {
            is TasksEvent.Modal.FabClicked -> _state.update {
                it.copy(
                    currentOverlay = TasksModalOverlay.CreateOptions,
                    isCreatingTask = false,
                    editingTaskId = null,
                    isDeleteModeActive = false
                )
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

    override fun onCleared() {
        super.onCleared()
        speechRecognizer.cancel()
    }

}