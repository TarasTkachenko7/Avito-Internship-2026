package com.example.avito_testing_2026_autum.notes.domain.model

data class Note(
    val id: Long,
    val title: String,
    val text: String = "",
    val imageUri: String? = null,
    val createdAt: Long
)