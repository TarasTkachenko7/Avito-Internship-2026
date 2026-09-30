package com.example.avito_testing_2026_autum.core.domain.manager

import java.io.File

interface FileManager {
    suspend fun createTempImageFile(prefix: String = "IMG_"): File
    suspend fun copyImageToInternalStorage(uriString: String, prefix: String = "IMG_"): String?
}