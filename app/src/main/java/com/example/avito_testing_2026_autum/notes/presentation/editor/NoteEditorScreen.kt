package com.example.avito_testing_2026_autum.notes.presentation.editor

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.avito_testing_2026_autum.R
import com.example.avito_testing_2026_autum.notes.presentation.components.editor.AttachmentDialog
import com.example.avito_testing_2026_autum.notes.presentation.components.editor.NoteAttachedImage
import com.example.avito_testing_2026_autum.notes.presentation.components.editor.NoteEditorTopBar
import com.example.avito_testing_2026_autum.notes.presentation.contract.editor.NoteEditorEffect
import com.example.avito_testing_2026_autum.notes.presentation.contract.editor.NoteEditorEvent
import com.example.avito_testing_2026_autum.notes.presentation.contract.editor.NoteEditorUiState
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

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                viewModel.handleEvent(NoteEditorEvent.OnCameraCaptureSuccess)
            }
        }
    )

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
            modifier = modifier,
        )
    }
}

@Composable
private fun NoteEditorScreenContent(
    state: NoteEditorUiState,
    onEvent: (NoteEditorEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                onEvent(NoteEditorEvent.OnImagePicked(uri.toString()))
            }
        }
    )

    Scaffold(
        modifier = modifier,
        topBar = {
            NoteEditorTopBar(
                isSaveButtonEnabled = state.isSaveButtonEnabled,
                onBackClick = { onEvent(NoteEditorEvent.OnBackClicked) },
                onSaveClick = { onEvent(NoteEditorEvent.OnSaveClicked) },
                onAddImageClick = { onEvent(NoteEditorEvent.OnAttachmentClicked) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            if (state.imageUri != null) {
                NoteAttachedImage(
                    imageUri = state.imageUri,
                    onRemoveClick = { onEvent(NoteEditorEvent.OnRemoveImageClicked) },
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            TextField(
                value = state.title,
                onValueChange = { onEvent(NoteEditorEvent.OnTitleChanged(it)) },
                placeholder = {
                    Text(
                        stringResource(R.string.title),
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                textStyle = MaterialTheme.typography.headlineMedium,
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            TextField(
                value = state.text,
                onValueChange = { onEvent(NoteEditorEvent.OnTextChanged(it)) },
                placeholder = { Text(stringResource(R.string.text)) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            )
        }
    }

    if (state.showAttachmentDialog) {
        AttachmentDialog(
            onDismiss = { onEvent(NoteEditorEvent.OnDismissAttachmentDialog) },
            onGalleryClick = {
                onEvent(NoteEditorEvent.OnGalleryClicked)
                photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            },
            onCameraClick = {
                onEvent(NoteEditorEvent.OnCameraClicked)
            }
        )
    }
}