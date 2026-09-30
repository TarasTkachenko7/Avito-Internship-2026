package com.example.avito_testing_2026_autum.tasks.presentation

import androidx.lifecycle.viewModelScope
import com.example.avito_testing_2026_autum.core.presentation.BaseViewModel
import com.example.avito_testing_2026_autum.tasks.domain.usecases.AddInlineTaskUseCase
import com.example.avito_testing_2026_autum.tasks.domain.usecases.DeleteTaskUseCase
import com.example.avito_testing_2026_autum.tasks.domain.usecases.GetTasksUseCase
import com.example.avito_testing_2026_autum.tasks.domain.usecases.ToggleTaskStatusUseCase
import com.example.avito_testing_2026_autum.tasks.presentation.contract.TasksEffect
import com.example.avito_testing_2026_autum.tasks.presentation.contract.TasksEvent
import com.example.avito_testing_2026_autum.tasks.presentation.contract.TasksUiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class TasksViewModel(
    private val getTasksUseCase: GetTasksUseCase,
    private val addInlineTaskUseCase: AddInlineTaskUseCase,
    private val toggleTaskStatusUseCase: ToggleTaskStatusUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase
) : BaseViewModel<TasksUiState, TasksEvent, TasksEffect>(
    initialValue = TasksUiState()
) {

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
                    setState { it.copy(tasks = tasksList, isLoading = false) }
                }
        }
    }

    override fun handleEvent(event: TasksEvent) {
        when (event) {
            is TasksEvent.OnSearchQueryChanged -> {
                setState { it.copy(searchQuery = event.query) }
            }
            is TasksEvent.OnSortClicked -> {
                setState { it.copy(sortType = event.sortType) }
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
                setState { it.copy(isCreatingTask = true, newTaskTitle = "") }
                sendEffect { TasksEffect.FocusOnNewTask }
            }
            is TasksEvent.OnNewTaskTitleChanged -> {
                setState { it.copy(newTaskTitle = event.title) }
            }
            is TasksEvent.OnSaveNewTask -> {
                viewModelScope.launch {
                    addInlineTaskUseCase(state.value.newTaskTitle)
                    setState { it.copy(isCreatingTask = false, newTaskTitle = "") }
                }
            }
            is TasksEvent.OnCancelNewTask -> {
                setState { it.copy(isCreatingTask = false, newTaskTitle = "") }
            }
        }
    }
}