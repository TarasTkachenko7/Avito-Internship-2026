package com.example.avito_testing_2026_autum.notes.presentation.editor

import android.Manifest
import android.content.pm.PackageManager
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.avito_testing_2026_autum.R
import com.example.avito_testing_2026_autum.notes.presentation.components.editor.NoteEditorImageCard
import com.example.avito_testing_2026_autum.notes.presentation.components.editor.NoteEditorInputCard
import com.example.avito_testing_2026_autum.notes.presentation.components.editor.NoteEditorTopBar
import com.example.avito_testing_2026_autum.notes.presentation.contract.editor.NoteEditorEffect
import com.example.avito_testing_2026_autum.notes.presentation.contract.editor.NoteEditorEvent
import com.example.avito_testing_2026_autum.notes.presentation.contract.editor.NoteEditorUiState
import com.example.avito_testing_2026_autum.voice.presentation.components.VoiceInputDialog
import org.koin.androidx.compose.koinViewModel
import java.io.File

@Composable
fun NoteEditorScreenRoot(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
    viewModel: NoteEditorViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                viewModel.handleEvent(NoteEditorEvent.Media.CameraCaptureSuccess)
            } else {
                viewModel.handleEvent(NoteEditorEvent.Media.CameraCaptureCancelled)
        }
        }
    )

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                runCatching {
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                }
                viewModel.handleEvent(NoteEditorEvent.Media.ImagePicked(uri.toString()))
            }
        }
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.handleEvent(NoteEditorEvent.Voice.VoiceInputClicked)
        } else {
            viewModel.handleEvent(NoteEditorEvent.Voice.PermissionDenied)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is NoteEditorEffect.NavigateBack -> onNavigateBack()
                is NoteEditorEffect.LaunchCamera -> {
                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        File(effect.uriString)
                    )
                    cameraLauncher.launch(uri)
                }

                is NoteEditorEffect.RequestMicrophonePermission -> {
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }

                is NoteEditorEffect.ShowMessage -> {
                    snackbarHostState.showSnackbar(effect.message.asString(context))
                }
            }
        }
    }

    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        NoteEditorScreenContent(
            state = state,
            onEvent = viewModel::handleEvent,
            snackbarHostState = snackbarHostState,
            onVoiceClick = {
                val hasPermission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
                if (hasPermission) {
                    viewModel.handleEvent(NoteEditorEvent.Voice.VoiceInputClicked)
                } else {
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            },
            onGalleryPick = {
                photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            },
            modifier = modifier,
        )
    }
}

@Composable
private fun NoteEditorScreenContent(
    state: NoteEditorUiState,
    onEvent: (NoteEditorEvent) -> Unit,
    snackbarHostState: SnackbarHostState,
    onVoiceClick: () -> Unit,
    onGalleryPick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    BackHandler(enabled = state.hasChanges) {
        focusManager.clearFocus()
        onEvent(NoteEditorEvent.Action.BackClicked)
    }

    if (state.showUnsavedChangesDialog) {
        AlertDialog(
            onDismissRequest = { onEvent(NoteEditorEvent.Dialog.DismissUnsavedDialog) },
            title = {
                Text(text = stringResource(R.string.unsaved_changes_title))
            },
            text = {
                Text(text = stringResource(R.string.unsaved_changes_message))
            },
            confirmButton = {
                TextButton(onClick = { onEvent(NoteEditorEvent.Action.DiscardAndExit) }) {
                    Text(
                        text = stringResource(R.string.action_exit),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(NoteEditorEvent.Dialog.DismissUnsavedDialog) }) {
                    Text(text = stringResource(R.string.action_cancel))
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            },
        topBar = {
            NoteEditorTopBar(
                isNewNote = state.originalCreatedAt == null,
                onBackClick = {
                    focusManager.clearFocus()
                    onEvent(NoteEditorEvent.Action.BackClicked)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            )
        },
        bottomBar = {
            if (!state.hasLoadError) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            onEvent(NoteEditorEvent.Action.SaveClicked)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = state.isSaveButtonEnabled,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.action_save),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (state.hasLoadError) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.note_editor_error_load),
                        color = MaterialTheme.colorScheme.error
                    )
                    Button(onClick = { onEvent(NoteEditorEvent.Action.RetryLoading) }) {
                        Text(stringResource(R.string.action_retry))
                    }
                }
            } else {
                NoteEditorInputCard(
                    title = state.title,
                    text = state.text,
                    onTitleChange = { onEvent(NoteEditorEvent.Input.TitleChanged(it)) },
                    onTextChange = { onEvent(NoteEditorEvent.Input.TextChanged(it)) },
                    onVoiceClick = onVoiceClick,
                    onClearTitle = { onEvent(NoteEditorEvent.Input.TitleChanged("")) }
                )
                NoteEditorImageCard(
                    imageUri = state.imageUri,
                    onGalleryClick = onGalleryPick,
                    onCameraClick = { onEvent(NoteEditorEvent.Dialog.CameraClicked) },
                    onRemoveClick = { onEvent(NoteEditorEvent.Action.RemoveImageClicked) }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (state.showVoiceDialog) {
        VoiceInputDialog(
            voiceState = state.voiceState,
            onStopListening = { onEvent(NoteEditorEvent.Voice.StopVoiceListening) },
            onDismiss = { onEvent(NoteEditorEvent.Dialog.DismissVoiceDialog) }
        )
    }
}
