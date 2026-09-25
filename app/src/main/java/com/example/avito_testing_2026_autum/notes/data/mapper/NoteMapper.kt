package com.example.avito_testing_2026_autum.notes.data.mapper

import com.example.avito_testing_2026_autum.notes.data.local.NoteEntity
import com.example.avito_testing_2026_autum.notes.domain.model.Note

fun NoteEntity.toDomain(): Note {
    return Note(
        id = id,
        title = title,
        text = text,
        imageUri = imageUri,
        createdAt = createdAt
    )
}

fun Note.toEntity(): NoteEntity {
    return NoteEntity(
        id = id,
        title = title,
        text = text,
        imageUri = imageUri,
        createdAt = createdAt
    )
}