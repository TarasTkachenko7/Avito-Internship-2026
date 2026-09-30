package com.example.avito_testing_2026_autum.tasks.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.avito_testing_2026_autum.tasks.presentation.components.TaskItem
import com.example.avito_testing_2026_autum.tasks.presentation.contract.TasksEffect
import com.example.avito_testing_2026_autum.tasks.presentation.contract.TasksEvent
import com.example.avito_testing_2026_autum.tasks.presentation.contract.TasksUiState
import org.koin.androidx.compose.koinViewModel
import com.example.avito_testing_2026_autum.R
import com.example.avito_testing_2026_autum.tasks.presentation.components.TasksTopBar

@Composable
fun TasksScreenRoot(
    viewModel: TasksViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is TasksEffect.FocusOnNewTask -> {
                    focusRequester.requestFocus()
                    keyboardController?.show()
                }
            }
        }
    }

    TasksScreenContent(
        state = state,
        focusRequester = focusRequester,
        onEvent = viewModel::handleEvent
    )
}

@Composable
private fun TasksScreenContent(
    state: TasksUiState,
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
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Добавить задачу")
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
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
                            onStatusChange = { isCompleted ->
                                onEvent(TasksEvent.OnCheckBoxClicked(task, isCompleted))
                            },
                            onDeleteClick = {
                                onEvent(TasksEvent.OnDeleteTaskClicked(task.id))
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
                keyboardActions = KeyboardActions(onDone = { onSave() }),
                singleLine = true
            )

            IconButton(onClick = onCancel) {
                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cancel))
            }
            IconButton(onClick = onSave, enabled = title.isNotBlank()) {
                Icon(Icons.Default.Check, contentDescription = stringResource(R.string.action_save))
            }
        }
    }
}