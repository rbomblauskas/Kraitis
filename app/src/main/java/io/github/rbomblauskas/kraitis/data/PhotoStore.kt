package io.github.rbomblauskas.kraitis.data

import android.content.Context
import android.net.Uri
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// keeps item photos in app internal storage so we own the files
class PhotoStore(private val context: Context) {
    private val photosDir: File
        get() = File(context.filesDir, "photos").apply { mkdirs() }

    fun newPhotoFile(): File = File(photosDir, "photo_${System.currentTimeMillis()}.jpg")

    // gallery uri access is temporary, so copy the image into our own file
    suspend fun copyFromUri(uri: Uri): String? = withContext(Dispatchers.IO) {
        runCatching {
            val file = newPhotoFile()
            val copied = context.contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
                true
            } ?: false
            if (copied) file.absolutePath else null
        }.getOrNull()
    }

    suspend fun delete(path: String?) = withContext(Dispatchers.IO) {
        if (path != null) {
            File(path).delete()
        }
    }
}
