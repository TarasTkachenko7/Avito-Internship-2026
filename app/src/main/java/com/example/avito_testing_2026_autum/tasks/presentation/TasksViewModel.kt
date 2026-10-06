package com.example.avito_testing_2026_autum.tasks.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.avito_testing_2026_autum.ai.domain.usecases.FormulateTaskUseCase
import com.example.avito_testing_2026_autum.tasks.domain.usecases.AddInlineTaskUseCase
import com.example.avito_testing_2026_autum.tasks.domain.usecases.DeleteTaskByIdUseCase
import com.example.avito_testing_2026_autum.tasks.domain.usecases.GetTasksUseCase
import com.example.avito_testing_2026_autum.tasks.domain.usecases.ToggleTaskStatusUseCase
import com.example.avito_testing_2026_autum.tasks.presentation.contract.TasksEffect
import com.example.avito_testing_2026_autum.tasks.presentation.contract.TasksEvent
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
    private val addInlineTaskUseCase: AddInlineTaskUseCase,
    private val toggleTaskStatusUseCase: ToggleTaskStatusUseCase,
    private val deleteTaskUseCase: DeleteTaskByIdUseCase,
    private val formulateTaskUseCase: FormulateTaskUseCase,
    private val speechRecognizer: SpeechRecognizerContract
) : ViewModel() {

    private val _state = MutableStateFlow(TasksUiState())
    val state: StateFlow<TasksUiState> = _state.asStateFlow()

    // Вспоминаем про паттерн из 1 итерации
    private val _effect = Channel<TasksEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        observeTasks()
        observeVoiceState()
    }

    private fun observeTasks() {
        // ... (оставляем старую реализацию observeTasks без изменений)
        state
            .map { it.appliedSearchQuery to it.sortOrder }
            .distinctUntilChanged()
            .flatMapLatest { (query, sortType) ->
                getTasksUseCase(query, sortType)
                    .map { domainTasks ->
                        domainTasks.map { it.toUiModel() }.toPersistentList()
                    }
                    .catch { exception ->
                        emit(persistentListOf())
                    }
            }
            .onEach { tasksList ->
                _state.update { it.copy(tasks = tasksList, isLoading = false) }
            }
            .launchIn(viewModelScope)
    }

    private fun observeVoiceState() {
        speechRecognizer.voiceState
            .onEach { voiceState ->
                when (voiceState) {
                    is VoiceState.Success -> {
                        _state.update {
                            it.copy(showVoiceDialog = false, isAiProcessing = true, voiceState = VoiceState.Idle)
                        }
                        speechRecognizer.reset()
                        processVoiceWithGigaChat(voiceState.text)
                    }
                    else -> {
                        _state.update { it.copy(voiceState = voiceState) }
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun processVoiceWithGigaChat(rawText: String) {
        viewModelScope.launch {
            formulateTaskUseCase(rawText)
                .onSuccess { formulatedText ->
                    addInlineTaskUseCase(formulatedText)
                    _state.update { it.copy(isAiProcessing = false) }
                }
                .onFailure { error ->
                    // Выводим полный стэк-трейс ошибки в консоль
                    Log.e("TasksViewModel", "GigaChat failed to formulate task", error)

                    _state.update { it.copy(isAiProcessing = false) }
                    sendEffect(TasksEffect.ShowError("Ошибка GigaChat: ${error.message ?: "Неизвестная ошибка"}"))
                }
        }
    }

    fun handleEvent(event: TasksEvent) {
        when (event) {
            is TasksEvent.OnFabClicked -> _state.update { it.copy(showCreateOptions = true) }
            is TasksEvent.OnDismissCreateOptions -> _state.update { it.copy(showCreateOptions = false) }
            is TasksEvent.OnTextTaskClicked -> {
                _state.update { it.copy(showCreateOptions = false, isCreatingTask = true, newTaskTitle = "") }
                sendEffect(TasksEffect.ScrollToTop)
                sendEffect(TasksEffect.FocusOnNewTask)
            }
            is TasksEvent.OnVoiceTaskClicked -> {
                _state.update { it.copy(showCreateOptions = false, showVoiceDialog = true) }
                speechRecognizer.startListening()
            }
            is TasksEvent.OnStopVoiceListening -> speechRecognizer.stopListening()
            is TasksEvent.OnDismissVoiceDialog -> {
                speechRecognizer.cancel()
                _state.update { it.copy(showVoiceDialog = false) }
            }
            is TasksEvent.OnPermissionDenied -> {
                sendEffect(TasksEffect.ShowError("Отсутствует разрешение на микрофон"))
            }

            // ... (оставляем старые обработчики)
            is TasksEvent.OnSearchQueryChanged -> _state.update { it.copy(searchQuery = event.query) }
            is TasksEvent.OnSearchClicked -> _state.update { it.copy(appliedSearchQuery = it.searchQuery) }
            is TasksEvent.OnSortClicked -> _state.update { it.copy(sortOrder = event.sortOrder) }
            is TasksEvent.OnFilterClicked -> _state.update { it.copy(filterType = event.filterType) }
            is TasksEvent.OnTaskStatusChanged -> viewModelScope.launch { toggleTaskStatusUseCase(event.taskId, event.isCompleted) }
            is TasksEvent.OnDeleteTaskClicked -> viewModelScope.launch { deleteTaskUseCase(event.taskId) }
            is TasksEvent.OnNewTaskTitleChanged -> _state.update { it.copy(newTaskTitle = event.title) }
            is TasksEvent.OnCancelNewTask -> _state.update { it.copy(isCreatingTask = false, newTaskTitle = "") }
            is TasksEvent.OnSaveNewTask -> {
                val titleToSave = state.value.newTaskTitle
                viewModelScope.launch {
                    val saved = addInlineTaskUseCase(titleToSave)
                    if (saved) {
                        _state.update { it.copy(isCreatingTask = false, newTaskTitle = "") }
                    } else {
                        sendEffect(TasksEffect.ShowError("Пустая задача"))
                    }
                }
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