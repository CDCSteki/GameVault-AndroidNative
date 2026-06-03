package com.example.gamevault.ui.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

fun createImageUri(context: Context): Uri {
    val imageFile = File(context.externalCacheDir, "profile_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        imageFile
    )
}

fun copyImageToInternalStorage(context: Context, sourceUri: Uri): String? {
    return try {
        val fileName = "profile_${System.currentTimeMillis()}.jpg"
        val destFile = File(context.filesDir, fileName)
        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            FileOutputStream(destFile).use { output ->
                input.copyTo(output)
            }
        }
        destFile.absolutePath
    } catch (e: Exception) {
        null
    }
}