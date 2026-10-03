package com.example.avito_testing_2026_autum.notes.presentation.mapper

import com.example.avito_testing_2026_autum.core.utils.toFormattedDateString
import com.example.avito_testing_2026_autum.notes.domain.model.Note
import com.example.avito_testing_2026_autum.notes.presentation.models.NoteUiModel

fun Note.toUiModel() = NoteUiModel(
    id = id,
    title = title,
    text = text,
    imageUri = imageUri,
    dateFormatted = createdAt.toFormattedDateString()
)