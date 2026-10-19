package com.example.avito_testing_2026_autum.notes.domain.usecases.editor

import com.example.avito_testing_2026_autum.core.domain.manager.FileManager

class DeleteImageUseCase(
    private val fileManager: FileManager
) {
    suspend operator fun invoke(uri: String): Boolean = fileManager.deleteFile(uri)
}
