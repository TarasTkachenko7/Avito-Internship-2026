package com.example.avito_testing_2026_autum.tasks.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.avito_testing_2026_autum.tasks.domain.usecases.AddInlineTaskUseCase
import com.example.avito_testing_2026_autum.tasks.domain.usecases.DeleteTaskByIdUseCase
import com.example.avito_testing_2026_autum.tasks.domain.usecases.GetTasksUseCase
import com.example.avito_testing_2026_autum.tasks.domain.usecases.ToggleTaskStatusUseCase
import com.example.avito_testing_2026_autum.tasks.presentation.contract.TasksEffect
import com.example.avito_testing_2026_autum.tasks.presentation.contract.TasksEvent
import com.example.avito_testing_2026_autum.tasks.presentation.contract.TasksUiState
import com.example.avito_testing_2026_autum.tasks.presentation.mapper.toUiModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
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
    private val deleteTaskUseCase: DeleteTaskByIdUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(TasksUiState())
    val state: StateFlow<TasksUiState> = _state.asStateFlow()

    private val _effect = Channel<TasksEffect>(
        capacity = 1,
        onBufferOverflow = BufferOverflow.DROP_LATEST
    )
    val effect = _effect.receiveAsFlow()

    init {
        observeTasks()
    }

    private fun observeTasks() {
        state
            .map { it.appliedSearchQuery to it.sortOrder }
            .distinctUntilChanged()
            .flatMapLatest { (query, sortType) ->
                getTasksUseCase(query, sortType)
                    .map { domainTasks ->
                        domainTasks.map { it.toUiModel() }.toPersistentList()
                    }
                    .catch { exception ->
                        Log.e("TasksViewModel", "Failed to observe tasks", exception)
                        emit(persistentListOf())
                    }
            }
            .onEach { tasksList ->
                _state.update { it.copy(tasks = tasksList, isLoading = false) }
            }
            .launchIn(viewModelScope)
    }

    fun handleEvent(event: TasksEvent) {
        when (event) {
            is TasksEvent.OnSearchQueryChanged -> {
                _state.update { it.copy(searchQuery = event.query) }
            }

            is TasksEvent.OnSearchClicked -> {
                _state.update { it.copy(appliedSearchQuery = it.searchQuery) }
            }

            is TasksEvent.OnSortClicked -> {
                _state.update { it.copy(sortOrder = event.sortOrder) }
            }

            is TasksEvent.OnFilterClicked -> {
                _state.update { it.copy(filterType = event.filterType) }
            }

            is TasksEvent.OnTaskStatusChanged -> {
                viewModelScope.launch {
                    toggleTaskStatusUseCase(event.taskId, event.isCompleted)
                }
            }

            is TasksEvent.OnDeleteTaskClicked -> {
                viewModelScope.launch {
                    deleteTaskUseCase(event.taskId)
                }
            }

            is TasksEvent.OnAddNewTaskClicked -> {
                _state.update { it.copy(isCreatingTask = true, newTaskTitle = "") }
                sendEffect(TasksEffect.ScrollToTop)
            }

            is TasksEvent.OnNewTaskTitleChanged -> {
                _state.update { it.copy(newTaskTitle = event.title) }
            }

            is TasksEvent.OnSaveNewTask -> {
                val titleToSave = state.value.newTaskTitle
                viewModelScope.launch {
                    val saved = addInlineTaskUseCase(titleToSave)
                    if (saved) {
                        _state.update { it.copy(isCreatingTask = false, newTaskTitle = "") }
                    } else {
                        sendEffect(TasksEffect.ShowError(""))
                    }
                }
            }

            is TasksEvent.OnCancelNewTask -> {
                _state.update { it.copy(isCreatingTask = false, newTaskTitle = "") }
            }
        }
    }

    private fun sendEffect(effect: TasksEffect) {
        viewModelScope.launch { _effect.send(effect) }
    }
}