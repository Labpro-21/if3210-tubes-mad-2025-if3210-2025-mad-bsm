package com.mad.besokminggu.ui.qr

import android.content.ClipData
import android.content.Context
import android.content.Context.WINDOW_SERVICE
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Point
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Display
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidmads.library.qrgenearator.QRGEncoder
import androidmads.library.qrgenearator.QRGContents
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.mad.besokminggu.databinding.FragmentQrBinding
import com.mad.besokminggu.manager.DeepLinkHelper
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class QrFragment : Fragment() {
    private var shareButton: Button? = null
    private lateinit var title: TextView
    private lateinit var artist: TextView
    private lateinit var qrImage: ImageView

    lateinit var bitmap: Bitmap
    lateinit var qrEncoder: QRGEncoder

    private var _binding: FragmentQrBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQrBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val songTitle = arguments?.getString("title") ?: "Song Title"
        val songArtist = arguments?.getString("artist") ?: "Song Artist"
        val songUrl = arguments?.getString("link") ?: "purritify://song/-1"

        title = binding.tvSongTitle
        artist = binding.tvSongArtist
        qrImage = binding.idIVQrcode

        shareButton = binding.dummyButton

        title.text = songTitle
        artist.text = songArtist

        // Create the QR code
        if (songUrl != "purritify://song/-1") {
            // on below line we are getting service for window manager
            val windowManager: WindowManager = getSystemService(requireContext(), WindowManager::class.java) as WindowManager

            val display: Display = windowManager.defaultDisplay

            val point: Point = Point()
            display.getSize(point)

            val width = point.x
            val height = point.y

            var dimen = if (width < height) width else height
            dimen = dimen * 3 / 4

            qrEncoder = QRGEncoder(songUrl, null, QRGContents.Type.TEXT, dimen)

            try {
                bitmap = qrEncoder.bitmap

                qrImage.setImageBitmap(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        shareButton?.setOnClickListener {
            val imageUri = saveBitmapAndGetUri(requireContext())

            // Start the share action
            val shareIntent = Intent.createChooser(
                Intent().apply {
                action = Intent.ACTION_SEND
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, imageUri)
//                putExtra(Intent.EXTRA_TEXT, "Check out this QR code for $songTitle by $songArtist\n${songUrl}")
                setClipData(ClipData.newRawUri(null, imageUri))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }, "Share QR Code")
            startActivity(shareIntent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun saveBitmapAndGetUri(context: Context): Uri? {
        val file = File(context.cacheDir, "qr_image.png")

        try {
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream)
            stream.flush()
            stream.close()

            // Get the URI using FileProvider
            return FileProvider.getUriForFile(
                context,
                context.applicationContext.packageName + ".provider",
                file
            )
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }
}