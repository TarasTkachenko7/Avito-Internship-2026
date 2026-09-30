package com.example.avito_testing_2026_autum.tasks.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.avito_testing_2026_autum.tasks.domain.usecases.AddInlineTaskUseCase
import com.example.avito_testing_2026_autum.tasks.domain.usecases.DeleteTaskUseCase
import com.example.avito_testing_2026_autum.tasks.domain.usecases.GetTasksUseCase
import com.example.avito_testing_2026_autum.tasks.domain.usecases.ToggleTaskStatusUseCase
import com.example.avito_testing_2026_autum.tasks.presentation.contract.TasksEffect
import com.example.avito_testing_2026_autum.tasks.presentation.contract.TasksEvent
import com.example.avito_testing_2026_autum.tasks.presentation.contract.TasksUiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class TasksViewModel(
    private val getTasksUseCase: GetTasksUseCase,
    private val addInlineTaskUseCase: AddInlineTaskUseCase,
    private val toggleTaskStatusUseCase: ToggleTaskStatusUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase
): ViewModel() {

    private val _state = MutableStateFlow(TasksUiState())
    val state: StateFlow<TasksUiState> = _state.asStateFlow()

    private val _effect = Channel<TasksEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        observeTasks()
    }

    private fun observeTasks() {
        viewModelScope.launch {
            state
                .map { it.searchQuery to it.sortType }
                .distinctUntilChanged()
                .flatMapLatest { (query, sortType) ->
                    getTasksUseCase(query, sortType)
                }
                .collect { tasksList ->
                    _state.update { it.copy(tasks = tasksList, isLoading = false) }
                }
        }
    }

    fun handleEvent(event: TasksEvent) {
        when (event) {
            is TasksEvent.OnSearchQueryChanged -> {
                _state.update { it.copy(searchQuery = event.query) }
            }
            is TasksEvent.OnSortClicked -> {
                _state.update { it.copy(sortType = event.sortType) }
            }
            is TasksEvent.OnCheckBoxClicked -> {
                viewModelScope.launch {
                    toggleTaskStatusUseCase(event.task, event.isCompleted)
                }
            }
            is TasksEvent.OnDeleteTaskClicked -> {
                viewModelScope.launch {
                    deleteTaskUseCase(event.taskId)
                }
            }
            is TasksEvent.OnAddNewTaskClicked -> {
                _state.update { it.copy(isCreatingTask = true, newTaskTitle = "") }
                sendEffect(TasksEffect.FocusOnNewTask)
            }
            is TasksEvent.OnNewTaskTitleChanged -> {
                _state.update { it.copy(newTaskTitle = event.title) }
            }
            is TasksEvent.OnSaveNewTask -> {
                viewModelScope.launch {
                    addInlineTaskUseCase(state.value.newTaskTitle)
                    _state.update { it.copy(isCreatingTask = false, newTaskTitle = "") }
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