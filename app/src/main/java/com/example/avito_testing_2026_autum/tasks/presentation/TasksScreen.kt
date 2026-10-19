package com.example.avito_testing_2026_autum.tasks.presentation

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Button
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
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
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

                    state.hasLoadError && state.tasks.isEmpty() && !state.isCreatingTask -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.align(Alignment.Center)) {
                            Text(stringResource(R.string.error_tasks_load))
                            Button(onClick = { onEvent(TasksEvent.Query.Retry) }) {
                                Text(stringResource(R.string.retry))
                            }
                        }
                    }

                    state.tasks.isEmpty() && !state.isCreatingTask -> {
                        EmptyTasksState(
                            isSearchActive = state.appliedSearchQuery.isNotEmpty(),
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    else -> {
                        Column {
                            if (state.hasLoadError) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                                ) {
                                    Text(stringResource(R.string.error_tasks_load), modifier = Modifier.weight(1f))
                                    TextButton(onClick = { onEvent(TasksEvent.Query.Retry) }) {
                                        Text(stringResource(R.string.retry))
                                    }
                                }
                            }
                            TasksList(
                                tasks = state.tasks,
                                isCreatingTask = state.isCreatingTask,
                                creationDraft = state.creationDraft,
                                isSavingCreation = state.isSavingCreation,
                                editingTaskId = state.editingTaskId,
                                editingDraftTitle = state.editingDraftTitle,
                                isSavingEdit = state.isSavingEdit,
                                isDeleteModeActive = state.isDeleteModeActive,
                                listState = listState,
                                onEvent = onEvent,
                                modifier = Modifier.weight(1f)
                            )
                        }
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
    creationDraft: String,
    isSavingCreation: Boolean,
    editingTaskId: Long?,
    editingDraftTitle: String,
    isSavingEdit: Boolean,
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
                    initialTitle = creationDraft,
                    isSaving = isSavingCreation,
                    onTitleChange = { onEvent(TasksEvent.Creation.DraftChanged(it)) },
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
                    initialTitle = editingDraftTitle,
                    isSaving = isSavingEdit,
                    onTitleChange = { onEvent(TasksEvent.Edit.DraftChanged(task.id, it)) },
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
                onDismissRequest = { onEvent(TasksEvent.Modal.DismissCurrentModal) },
                properties = DialogProperties(
                    dismissOnBackPress = true,
                    dismissOnClickOutside = true
                )
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                        .padding(24.dp)
                ) {
                    CircularProgressIndicator()
                    TextButton(onClick = { onEvent(TasksEvent.Modal.DismissCurrentModal) }) {
                        Text("Отменить")
                    }
                }
            }
        }

        TasksModalOverlay.AiSaveFailed -> {
            AlertDialog(
                onDismissRequest = { onEvent(TasksEvent.Modal.DismissCurrentModal) },
                title = { Text("Не удалось сохранить задачу") },
                text = { Text("Текст задачи сохранён для повтора.") },
                confirmButton = {
                    TextButton(onClick = { onEvent(TasksEvent.Modal.RetryAiSave) }) {
                        Text("Повторить сохранение")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { onEvent(TasksEvent.Modal.DismissCurrentModal) }) {
                        Text("Отмена")
                    }
                }
            )
        }

        null -> Unit
    }
}
