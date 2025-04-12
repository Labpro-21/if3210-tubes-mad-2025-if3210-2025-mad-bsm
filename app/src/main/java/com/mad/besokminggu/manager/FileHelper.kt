package com.mad.besokminggu.manager

import android.content.Context
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object FileHelper {

    private lateinit var appContext : Context;

    private val allowedSubDirs = setOf("audio", "cover")

    fun init(context : Context){
        appContext = context
    }

    private fun validateDir(subDir : String): File{
        require(::appContext.isInitialized) { "FileHelper must be initialized with context" }
        require(allowedSubDirs.contains(subDir)) { "Subdirectory '$subDir' is not allowed." }

        val dir = File(appContext.filesDir, subDir)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun saveFile(bytes: ByteArray, fileName : String, subDir : String): File?{
        val dir = validateDir(subDir)
        val file = File(dir, fileName)
        return try {
            FileOutputStream(file).use { it.write(bytes) }
            file
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    fun saveFileGenerated(
        bytes: ByteArray,
        extension: String,
        subDir: String,
        prefix: String = "file_"
    ): File? {
        return try {
            val dir = validateDir(subDir) // helper to ensure valid path
            if (!dir.exists()) dir.mkdirs()

            val fileName = "$prefix${System.currentTimeMillis()}$extension"
            val file = File(dir, fileName)
            file.writeBytes(bytes)
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    fun getFile(fileName: String, subDir: String): File {
        val file =File(validateDir(subDir), fileName)
        if(!file.exists()){
            Log.e("FILE","file with directory of ${file.absolutePath} aren't found")
        }
        return file;
    }

    fun getFileExtension(context: Context, uri: Uri): String {
        val type = context.contentResolver.getType(uri)
        return if (type != null) {
            MimeTypeMap.getSingleton().getExtensionFromMimeType(type) ?: "dat"
        } else {
            val path = uri.toString()
            path.substringAfterLast('.', "dat")
        }
    }

    fun deleteFile(fileName: String, subDir: String): Boolean {
        val file = File(validateDir(subDir), fileName)
        return file.exists() && file.delete()
    }
}