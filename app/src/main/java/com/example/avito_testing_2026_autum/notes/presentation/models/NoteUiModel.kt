package com.example.avito_testing_2026_autum.notes.presentation.models

import androidx.compose.runtime.Immutable

@Immutable
data class NoteUiModel(
    val id: Long,
    val title: String,
    val text: String,
    val imageUri: String?,
    val dateFormatted: String
)