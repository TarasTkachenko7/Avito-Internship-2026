package com.example.avito_testing_2026_autum.tasks.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.example.avito_testing_2026_autum.R
import com.example.avito_testing_2026_autum.tasks.presentation.components.TaskItem
import com.example.avito_testing_2026_autum.tasks.presentation.components.TasksTopBar
import com.example.avito_testing_2026_autum.tasks.presentation.contract.TasksEffect
import com.example.avito_testing_2026_autum.tasks.presentation.contract.TasksEvent
import com.example.avito_testing_2026_autum.tasks.presentation.contract.TasksUiState
import org.koin.androidx.compose.koinViewModel

@Composable
fun TasksScreenRoot(
    viewModel: TasksViewModel = koinViewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val listState = rememberLazyListState()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(viewModel.effect, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effect.collect { effect ->
                when (effect) {
                    is TasksEffect.FocusOnNewTask -> {
                        focusRequester.requestFocus()
                        keyboardController?.show()
                    }

                    is TasksEffect.ScrollToTop -> {
                        listState.animateScrollToItem(0)
                    }

                    is TasksEffect.ShowError -> {

                    }
                }
            }
        }
    }

    TasksScreenContent(
        state = state,
        listState = listState,
        focusRequester = focusRequester,
        onEvent = viewModel::handleEvent
    )
}

@Composable
private fun TasksScreenContent(
    state: TasksUiState,
    listState: LazyListState,
    focusRequester: FocusRequester,
    onEvent: (TasksEvent) -> Unit
) {
    Scaffold(
        topBar = {
            TasksTopBar(
                searchQuery = state.searchQuery,
                onSearchQueryChange = { onEvent(TasksEvent.OnSearchQueryChanged(it)) },
                onSortSelect = { onEvent(TasksEvent.OnSortClicked(it)) }
            )
        },
        floatingActionButton = {
            if (!state.isCreatingTask) {
                FloatingActionButton(onClick = { onEvent(TasksEvent.OnAddNewTaskClicked) }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.add_task)
                    )
                }
            }
        }
    ) { paddingValues ->
        TasksList(
            state = state,
            listState = listState,
            focusRequester = focusRequester,
            onEvent = onEvent,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
private fun TasksList(
    state: TasksUiState,
    listState: LazyListState,
    focusRequester: FocusRequester,
    onEvent: (TasksEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        when {
            state.isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            state.tasks.isEmpty() && !state.isCreatingTask -> {
                Text(
                    text = stringResource(R.string.no_tasks),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listState,
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    if (state.isCreatingTask) {
                        item {
                            InlineTaskInput(
                                title = state.newTaskTitle,
                                focusRequester = focusRequester,
                                onTitleChange = { onEvent(TasksEvent.OnNewTaskTitleChanged(it)) },
                                onSave = { onEvent(TasksEvent.OnSaveNewTask) },
                                onCancel = { onEvent(TasksEvent.OnCancelNewTask) }
                            )
                        }
                    }

                    items(
                        items = state.tasks,
                        key = { task -> task.id }
                    ) { task ->
                        TaskItem(
                            task = task,
                            onStatusChange = { taskId, isCompleted ->
                                onEvent(TasksEvent.OnTaskStatusChanged(taskId, isCompleted))
                            },
                            onDeleteClick = { taskId ->
                                onEvent(TasksEvent.OnDeleteTaskClicked(taskId))
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InlineTaskInput(
    title: String,
    focusRequester: FocusRequester,
    onTitleChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = title,
                onValueChange = onTitleChange,
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester),
                placeholder = { Text(stringResource(R.string.new_task)) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        keyboardController?.hide()
                        onSave()
                    }
                ),
                singleLine = true
            )

            IconButton(onClick = {
                keyboardController?.hide()
                onCancel()
            }) {
                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cancel))
            }
            IconButton(
                onClick = {
                    keyboardController?.hide()
                    onSave()
                },
                enabled = title.isNotBlank()
            ) {
                Icon(Icons.Default.Check, contentDescription = stringResource(R.string.action_save))
            }
        }
    }
}