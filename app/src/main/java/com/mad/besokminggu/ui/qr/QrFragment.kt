package com.mad.besokminggu.ui.qr

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

/**
 * An example full-screen fragment that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class QrFragment : Fragment() {
    private val hideHandler = Handler(Looper.myLooper()!!)

    @Suppress("InlinedApi")
    private val hidePart2Runnable = Runnable {
        // Delayed removal of status and navigation bar

        // Note that some of these constants are new as of API 16 (Jelly Bean)
        // and API 19 (KitKat). It is safe to use them, as they are inlined
        // at compile-time and do nothing on earlier devices.
        val flags =
            View.SYSTEM_UI_FLAG_LOW_PROFILE or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        activity?.window?.decorView?.systemUiVisibility = flags
        (activity as? AppCompatActivity)?.supportActionBar?.hide()
    }
    private val showPart2Runnable = Runnable {
        // Delayed display of UI elements
        fullscreenContentControls?.visibility = View.VISIBLE
    }

    private var shareButton: Button? = null
    private var fullscreenContentControls: View? = null
    private lateinit var title: TextView
    private lateinit var artist: TextView
    private lateinit var qrImage: ImageView

    lateinit var bitmap: Bitmap
    lateinit var qrEncoder: QRGEncoder

    private var _binding: FragmentQrBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentQrBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val songTitle = arguments?.getString("title") ?: "Song Title"
        val songArtist = arguments?.getString("artist") ?: "Song Artist"
        val songUrl = arguments?.getString("link") ?: "purritify://song/0"

        title = binding.tvSongTitle
        artist = binding.tvSongArtist
        qrImage = binding.idIVQrcode

        shareButton = binding.dummyButton
        fullscreenContentControls = binding.fullscreenContentControls

        title.text = songTitle
        artist.text = songArtist

        // Create the QR code
        if (songUrl != "purritify://song/0") {
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
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }, "Share QR Code")
            startActivity(shareIntent)
        }
    }

    override fun onResume() {
        super.onResume()
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
    }

    override fun onPause() {
        super.onPause()
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        // Clear the systemUiVisibility flag
        activity?.window?.decorView?.systemUiVisibility = 0
    }

    override fun onDestroy() {
        super.onDestroy()
        fullscreenContentControls = null
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