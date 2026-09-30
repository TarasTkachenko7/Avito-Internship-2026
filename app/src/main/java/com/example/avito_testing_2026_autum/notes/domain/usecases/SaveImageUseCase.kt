package com.example.avito_testing_2026_autum.notes.domain.usecases

import android.net.Uri
import com.example.avito_testing_2026_autum.core.domain.manager.FileManager

class SaveImageUseCase(private val fileRepository: FileManager) {
    suspend operator fun invoke(uri: Uri): String? {
        return fileRepository.copyImageToInternalStorage(uri, prefix = "NOTE_IMG_")
    }
}