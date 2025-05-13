package com.mad.besokminggu.manager


import java.io.File

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

}
