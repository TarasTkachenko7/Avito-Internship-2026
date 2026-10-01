package com.example.avito_testing_2026_autum.notes.domain.usecases.editor

import com.example.avito_testing_2026_autum.core.domain.manager.FileManager

class SaveImageUseCase(
    private val fileRepository: FileManager
) {
    suspend operator fun invoke(uri: String): String? {
        return fileRepository.copyImageToInternalStorage(uri, prefix = "NOTE_IMG_")
    }
}