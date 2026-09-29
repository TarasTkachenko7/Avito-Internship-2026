package com.example.avito_testing_2026_autum.notes.presentation.editor

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.avito_testing_2026_autum.notes.presentation.components.editor.NoteAttachedImage
import com.example.avito_testing_2026_autum.notes.presentation.components.editor.NoteEditorTopBar
import com.example.avito_testing_2026_autum.notes.presentation.contract.editor.NoteEditorEffect
import com.example.avito_testing_2026_autum.notes.presentation.contract.editor.NoteEditorEvent
import com.example.avito_testing_2026_autum.notes.presentation.contract.editor.NoteEditorUiState
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import com.example.avito_testing_2026_autum.R
import com.example.avito_testing_2026_autum.core.utils.copyImageToInternalStorage
import com.example.avito_testing_2026_autum.core.utils.createTempImageFile
import com.example.avito_testing_2026_autum.notes.presentation.components.editor.AttachmentDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun NoteEditorScreenRoot(
    noteId: Long,
    onNavigateBack: () -> Unit,
    viewModel: NoteEditorViewModel = koinViewModel { parametersOf(noteId) }
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is NoteEditorEffect.NavigateBack -> onNavigateBack()
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
            onEvent = viewModel::handleEvent
        )
    }
}

@Composable
private fun NoteEditorScreenContent(
    state: NoteEditorUiState,
    onEvent: (NoteEditorEvent) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var tempImageUri by rememberSaveable { mutableStateOf<android.net.Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success && tempImageUri != null) {
                coroutineScope.launch(Dispatchers.IO) {
                    val permanentPath = context.copyImageToInternalStorage(tempImageUri!!)
                    withContext(Dispatchers.Main) {
                        onEvent(NoteEditorEvent.OnImageSelected(permanentPath))
                    }
                }
            }
        }
    )

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                coroutineScope.launch(Dispatchers.IO) {
                    val permanentPath = context.copyImageToInternalStorage(uri)
                    withContext(Dispatchers.Main) {
                        onEvent(NoteEditorEvent.OnImageSelected(permanentPath))
                    }
                }
            }
        }
    )

    Scaffold(
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
                    onRemoveClick = { onEvent(NoteEditorEvent.OnImageSelected(null)) }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            TextField(
                value = state.title,
                onValueChange = { onEvent(NoteEditorEvent.OnTitleChanged(it)) },
                placeholder = {
                    Text(stringResource(R.string.title), style = MaterialTheme.typography.headlineMedium)
                },
                textStyle = MaterialTheme.typography.headlineMedium,
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            )

            TextField(
                value = state.text,
                onValueChange = { onEvent(NoteEditorEvent.OnTextChanged(it)) },
                placeholder = { Text(stringResource(R.string.text)) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }
    }

    if (state.showAttachmentDialog) {
        AttachmentDialog(
            onDismiss = { onEvent(NoteEditorEvent.OnDismissAttachmentDialog) },
            onGalleryClick = {
                photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            },
            onCameraClick = {
                val tempFile = context.createTempImageFile()

                val uri = androidx.core.content.FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    tempFile
                )

                tempImageUri = uri
                cameraLauncher.launch(uri)
            }
        )
    }
}