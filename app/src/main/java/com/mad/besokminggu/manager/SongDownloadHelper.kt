package com.mad.besokminggu.manager

import android.content.Context
import com.mad.besokminggu.manager.FileHelper.validateDir
import java.io.File
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.FileOutputStream
import java.io.IOException

object SongDownloadHelper {
    fun downloadFile(
        url: String,
        prefix: String = "file_",
        subDir: String,
        ext: String,
    ): String {
        try {
            val dir = validateDir(subDir) // Ensure this returns File(Environment.getExternalStoragePublicDirectory(...))
            if (!dir.exists()) dir.mkdirs()

            val client = OkHttpClient()

            val request = Request.Builder()
                .url(url)
                .build()

            val fileName = "$prefix${System.currentTimeMillis()}.$ext"
            val file = File(dir, fileName)

            client.newCall(request).enqueue(object : okhttp3.Callback {
                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace()
                }

                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        println("Download failed: ${response.code}")
                        return
                    }

                    response.body?.byteStream()?.use { input ->
                        FileOutputStream(file).use { output ->
                            input.copyTo(output)
                        }
                    }
                }
            })

            return fileName

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return ""
    }
}