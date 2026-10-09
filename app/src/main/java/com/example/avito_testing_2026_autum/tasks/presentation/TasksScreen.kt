package com.example.avito_testing_2026_autum.tasks.presentation

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.example.avito_testing_2026_autum.R
import com.example.avito_testing_2026_autum.tasks.presentation.components.EmptyTasksState
import com.example.avito_testing_2026_autum.tasks.presentation.components.TaskFilterRow
import com.example.avito_testing_2026_autum.tasks.presentation.components.TaskInlineInputItem
import com.example.avito_testing_2026_autum.tasks.presentation.components.TaskItem
import com.example.avito_testing_2026_autum.tasks.presentation.components.TasksSearchBar
import com.example.avito_testing_2026_autum.tasks.presentation.contract.TasksEffect
import com.example.avito_testing_2026_autum.tasks.presentation.contract.TasksEvent
import com.example.avito_testing_2026_autum.tasks.presentation.contract.TasksModalOverlay
import com.example.avito_testing_2026_autum.tasks.presentation.contract.TasksUiState
import com.example.avito_testing_2026_autum.tasks.presentation.models.TaskUiModel
import com.example.avito_testing_2026_autum.voice.presentation.components.VoiceInputDialog
import kotlinx.collections.immutable.ImmutableList
import org.koin.androidx.compose.koinViewModel

@Composable
fun TasksScreenRoot(
    viewModel: TasksViewModel = koinViewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val snackbarHostState = remember { SnackbarHostState() }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.handleEvent(TasksEvent.Modal.StartVoice)
        } else {
            viewModel.handleEvent(TasksEvent.Modal.PermissionDenied)
        }
    }

    LaunchedEffect(viewModel.effect, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effect.collect { effect ->
                when (effect) {
                    is TasksEffect.FocusOnNewTask -> {
                        keyboardController?.show()
                    }

                    is TasksEffect.ScrollToTop -> {
                        listState.animateScrollToItem(0)
                    }

                    is TasksEffect.ShowError -> {
                        snackbarHostState.showSnackbar(effect.message.asString(context))
                    }

                    is TasksEffect.RequestRecordAudioPermission -> {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                }
            }
        }
    }

    TasksScreenContent(
        state = state,
        listState = listState,
        snackbarHostState = snackbarHostState,
        onEvent = viewModel::handleEvent,
        onVoiceRequest = {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED

            if (hasPermission) {
                viewModel.handleEvent(TasksEvent.Modal.StartVoice)
            } else {
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        },
        modifier = modifier
    )
}

@Composable
private fun TasksScreenContent(
    state: TasksUiState,
    listState: LazyListState,
    snackbarHostState: SnackbarHostState,
    onEvent: (TasksEvent) -> Unit,
    onVoiceRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    var searchQueryInput by rememberSaveable { mutableStateOf(state.appliedSearchQuery) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
            detectTapGestures(onTap = {
                focusManager.clearFocus()
            })
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (!state.isCreatingTask && !state.isDeleteModeActive) {
                FloatingActionButton(
                    onClick = { onEvent(TasksEvent.Modal.FabClicked) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.add_task)
                    )
                }
            }
        }
    ) { _ ->
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Text(
                text = stringResource(R.string.tasks),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
            )

            TasksSearchBar(
                query = searchQueryInput,
                isDeleteModeActive = state.isDeleteModeActive,
                enabled = !state.isCreatingTask,
                onQueryChange = { searchQueryInput = it },
                onSearchTriggered = {
                    onEvent(TasksEvent.Query.SearchSubmit(searchQueryInput))
                },
                onClearQuery = {
                    searchQueryInput = ""
                    onEvent(TasksEvent.Query.SearchClear)
                },
                onToggleDeleteMode = {
                    onEvent(TasksEvent.Action.ToggleDeleteMode)
                }
            )

            TaskFilterRow(
                selectedFilter = state.filterType,
                selectedSort = state.sortOrder,
                onFilterSelected = { onEvent(TasksEvent.Query.FilterSelected(it)) },
                onSortSelected = { onEvent(TasksEvent.Query.SortSelected(it)) }
            )

            Spacer(modifier = Modifier.height(4.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when {
                    state.isLoading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }

                    state.tasks.isEmpty() && !state.isCreatingTask -> {
                        EmptyTasksState(
                            isSearchActive = state.appliedSearchQuery.isNotEmpty(),
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    else -> {
                        TasksList(
                            tasks = state.tasks,
                            isCreatingTask = state.isCreatingTask,
                            editingTaskId = state.editingTaskId,
                            isDeleteModeActive = state.isDeleteModeActive,
                            listState = listState,
                            onEvent = onEvent
                        )
                    }
                }
            }
        }
    }

    TasksOverlays(
        currentOverlay = state.currentOverlay,
        onEvent = onEvent,
        onVoiceRequest = onVoiceRequest
    )
}

@Composable
private fun TasksList(
    tasks: ImmutableList<TaskUiModel>,
    isCreatingTask: Boolean,
    editingTaskId: Long?,
    isDeleteModeActive: Boolean,
    listState: LazyListState,
    onEvent: (TasksEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(top = 4.dp, bottom = 100.dp)
    ) {
        if (isCreatingTask) {
            item(key = "inline_input_key") {
                TaskInlineInputItem(
                    onSave = { onEvent(TasksEvent.Creation.Save(it)) },
                    onCancel = { onEvent(TasksEvent.Creation.Cancel) }
                )
            }
        }

        items(
            items = tasks,
            key = { task -> task.id }
        ) { task ->
            if (editingTaskId == task.id) {
                TaskInlineInputItem(
                    initialTitle = task.title,
                    onSave = { onEvent(TasksEvent.Edit.Save(task.id, it)) },
                    onCancel = { onEvent(TasksEvent.Edit.Cancel) }
                )
            } else {
                TaskItem(
                    modifier = Modifier.animateItem(),
                    task = task,
                    isDeleteModeActive = isDeleteModeActive,
                    enabled = !isCreatingTask && editingTaskId == null,
                    onStatusChange = { taskId, isCompleted ->
                        onEvent(TasksEvent.Action.StatusChanged(taskId, isCompleted))
                    },
                    onDeleteClick = { taskId ->
                        onEvent(TasksEvent.Action.DeleteClicked(taskId))
                    },
                    onTaskClick = {
                        onEvent(TasksEvent.Edit.Start(task.id))
                    }
                )
            }
        }
    }
}

@Composable
private fun TasksOverlays(
    currentOverlay: TasksModalOverlay?,
    onEvent: (TasksEvent) -> Unit,
    onVoiceRequest: () -> Unit
) {
    when (currentOverlay) {
        is TasksModalOverlay.CreateOptions -> {
            AlertDialog(
                onDismissRequest = { onEvent(TasksEvent.Modal.DismissCurrentModal) },
                title = { Text(stringResource(R.string.add_task)) },
                text = { Text("Выберите способ создания задачи") },
                confirmButton = {
                    TextButton(onClick = {
                        onEvent(TasksEvent.Modal.DismissCurrentModal)
                        onVoiceRequest()
                    }) {
                        Text("Голосовой ввод")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        onEvent(TasksEvent.Creation.Start)
                    }) {
                        Text("Текстовый ввод")
                    }
                }
            )
        }

        is TasksModalOverlay.VoiceInput -> {
            VoiceInputDialog(
                voiceState = currentOverlay.voiceState,
                onStopListening = { onEvent(TasksEvent.Modal.VoiceConfirm) },
                onDismiss = { onEvent(TasksEvent.Modal.DismissCurrentModal) }
            )
        }

        is TasksModalOverlay.AiProcessing -> {
            Dialog(
                onDismissRequest = {},
                properties = DialogProperties(
                    dismissOnBackPress = false,
                    dismissOnClickOutside = false
                )
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(100.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(16.dp)
                        )
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        null -> Unit
    }
}