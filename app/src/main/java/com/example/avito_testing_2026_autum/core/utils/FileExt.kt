package com.example.avito_testing_2026_autum.core.utils

import android.content.Context
import android.net.Uri
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Context.createTempImageFile(): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val imageFileName = "JPEG_${timeStamp}_"

    return File.createTempFile(
        imageFileName,
        ".jpg",
        cacheDir
    )
}

fun Context.copyImageToInternalStorage(uri: Uri): String? {
    return try {
        val inputStream = contentResolver.openInputStream(uri) ?: return null

        val fileName = "NOTE_IMG_${System.currentTimeMillis()}.jpg"

        val file = File(filesDir, fileName)
        val outputStream = file.outputStream()

        inputStream.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }

        file.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}