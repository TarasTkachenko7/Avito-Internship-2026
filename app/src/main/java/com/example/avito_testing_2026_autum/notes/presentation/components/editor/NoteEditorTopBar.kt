package com.example.avito_testing_2026_autum.notes.presentation.components.editor

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.avito_testing_2026_autum.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorTopBar(
    isSaveButtonEnabled: Boolean,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit,
    onAddImageClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {},
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.action_cancel)
                )
            }
        },
        actions = {
            IconButton(onClick = onAddImageClick) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = stringResource(R.string.add_photo)
                )
            }
            TextButton(
                onClick = onSaveClick,
                enabled = isSaveButtonEnabled
            ) {
                Text(text = stringResource(R.string.action_save))
            }
        },
        modifier = modifier
    )
}