package com.mad.besokminggu.ui.capsule

import MonthlySummaryCapsule
import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import java.io.File
import java.io.FileOutputStream

object PdfGenerator {
    fun generateMultiMonthPDF(
        context: Context,
        summaries: List<MonthlySummaryCapsule>,
        streakMap: Map<String, String>,
        chartMap: Map<String, Bitmap?>
    ): File {
        val pdfDocument = PdfDocument()
        val paint = Paint().apply {
            color = Color.BLACK
            textSize = 16f
            isAntiAlias = true
        }

        summaries.forEachIndexed { index, capsule ->
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, index + 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            var y = 50
            canvas.drawText("Sound Capsule - ${capsule.month}", 180f, y.toFloat(), paint)

            y += 40
            canvas.drawText("1. Time Listened: ${capsule.totalMinutes ?: 0} minutes", 40f, y.toFloat(), paint)

            y += 30
            canvas.drawText("2. Top 5 Songs:", 40f, y.toFloat(), paint)
            capsule.topSongsList.take(5).forEachIndexed { i, song ->
                y += 25
                canvas.drawText("${i + 1}. $song", 60f, y.toFloat(), paint)
            }

            y += 30
            canvas.drawText("3. Top 3 Artists:", 40f, y.toFloat(), paint)
            capsule.topArtistsList.take(3).forEachIndexed { i, artist ->
                y += 25
                canvas.drawText("${i + 1}. $artist", 60f, y.toFloat(), paint)
            }

            y += 30
            canvas.drawText("4. Streak Info:", 40f, y.toFloat(), paint)
            canvas.drawText(streakMap[capsule.month] ?: "No data available", 60f, y + 25f, paint)

            // Time Listened Chart
            chartMap[capsule.month]?.let { chart ->
                val scaled = Bitmap.createScaledBitmap(chart, 400, 200, true)
                y += 80


                canvas.drawText("5. Time Listened per Week Visualization:", 40f, y.toFloat(), paint)
                canvas.drawBitmap(scaled, 80f, y + 10f, null)
            }

            pdfDocument.finishPage(page)
        }

        val file = File(context.cacheDir, "sound_capsule_all_months.pdf")
        pdfDocument.writeTo(FileOutputStream(file))
        pdfDocument.close()
        return file
    }



    private fun getCurrentMonthLabel(): String {
        val sdf = java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale.getDefault())
        return sdf.format(java.util.Date())
    }
}
