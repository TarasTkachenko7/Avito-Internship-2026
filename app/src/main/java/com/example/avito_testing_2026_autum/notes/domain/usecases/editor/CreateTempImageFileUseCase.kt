package com.example.avito_testing_2026_autum.notes.domain.usecases.editor

import com.example.avito_testing_2026_autum.core.domain.manager.FileManager
import java.io.File

class CreateTempImageFileUseCase(private val fileManager: FileManager) {
    suspend operator fun invoke(): File {
        return fileManager.createTempImageFile(prefix = "TEMP_CAMERA_IMG_")
    }
}