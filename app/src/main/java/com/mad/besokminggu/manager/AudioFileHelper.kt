package com.mad.besokminggu.manager


import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream

object AudioFileHelper {

    private const val SUB_DIR = "audio"

    fun getFile(fileName: String): File? {
        val file = FileHelper.getFile(fileName, SUB_DIR)
        return if (file.exists()) file else null
    }

//    fun saveFile(bytes: ByteArray, fileName: String): File? {
//        return FileHelper.saveFile(bytes, fileName, SUB_DIR)
//    }

    fun saveGeneratedFile(bytes: ByteArray, extension: String, prefix: String = "audio_"): File? {
        return FileHelper.saveFileGenerated(bytes, extension, SUB_DIR, prefix)
    }

    fun deleteFile(fileName: String){
        FileHelper.deleteFile(fileName, SUB_DIR)
    }

    fun getUri(context: Context, fileName: String): Uri? {
        val file = getFile(fileName) ?: return null
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    fun loadBytesFromUri(context: Context, uri: Uri): ByteArray? =
        runCatching {
            context.contentResolver.openInputStream(uri)?.use { it.readAllBytes() }
        }.getOrNull()

    private fun InputStream.readAllBytes(): ByteArray {
        val buffer = ByteArrayOutputStream()
        copyTo(buffer)
        return buffer.toByteArray()
    }
}
