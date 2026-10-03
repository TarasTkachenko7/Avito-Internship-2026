package com.example.avito_testing_2026_autum.core.data.local.files

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import com.example.avito_testing_2026_autum.core.dispatchers.DispatchersProvider
import com.example.avito_testing_2026_autum.core.domain.manager.FileManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.UUID

class FileManagerImpl(
    context: Context,
    private val dispatchers: DispatchersProvider
) : FileManager {
    private val appContext = context.applicationContext

    override suspend fun createTempImageFile(prefix: String): File = withContext(dispatchers.io) {
        val storageDir = appContext.cacheDir.apply { mkdirs() }
        File.createTempFile(prefix, ".jpg", storageDir)
    }

    override suspend fun copyImageToInternalStorage(uriString: String, prefix: String): String? {
        return withContext(dispatchers.io) {
            try {
                val inputStream = if (uriString.startsWith("/")) {
                    FileInputStream(File(uriString))
                } else {
                    appContext.contentResolver.openInputStream(uriString.toUri())
                } ?: return@withContext null

                val fileName = "${prefix}${System.currentTimeMillis()}_${UUID.randomUUID()}.jpg"
                val filesDirectory = appContext.filesDir.apply { mkdirs() }
                val destinationFile = File(filesDirectory, fileName)

                inputStream.use { input ->
                    FileOutputStream(destinationFile).use { output ->
                        input.copyTo(output)
                    }
                }

                Uri.fromFile(destinationFile).toString()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.e("FileManagerImpl", "Failed to copy image to internal storage", e)
                null
            }
        }
    }
}