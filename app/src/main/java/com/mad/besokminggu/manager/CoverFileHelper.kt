package com.mad.besokminggu.manager

import java.io.File

object CoverFileHelper {

    private const val SUB_DIR = "cover"

    fun getFile(fileName: String): File? {
        val file = FileHelper.getFile(fileName, SUB_DIR)
        return if (file.exists()) file else null
    }

    fun saveFile(bytes: ByteArray, fileName: String): File? {
        return FileHelper.saveFile(bytes, fileName, SUB_DIR)
    }

    fun saveGeneratedFile(bytes: ByteArray, extension: String, prefix: String = "cover_"): File? {
        return FileHelper.saveFileGenerated(bytes, extension, SUB_DIR, prefix)
    }

    fun deleteFile(fileName: String){
        FileHelper.deleteFile(fileName, SUB_DIR)
    }
}
