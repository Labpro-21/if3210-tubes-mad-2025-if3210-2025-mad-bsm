package com.mad.besokminggu.ui.qr

import android.animation.Animator
import android.animation.AnimatorInflater
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.mad.besokminggu.R
import com.mad.besokminggu.databinding.FragmentProfileBinding
import com.mad.besokminggu.databinding.FragmentScannerBinding
import dagger.hilt.android.AndroidEntryPoint
import androidx.core.net.toUri

val VIBRATE_DURATION = 50L

@AndroidEntryPoint
class ScannerFragment : Fragment() {

    private val qrCodeViewModel: ScannerViewModel by viewModels()

    private val vibrator: Vibrator by lazy {
        requireActivity().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    /**
     *  Vibration mobile on Scan successful.
     */
    private fun vibrateOnScan() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(
                        VIBRATE_DURATION,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            } else {
                vibrator.vibrate(VIBRATE_DURATION)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private var _binding: FragmentScannerBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScannerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        qrCodeViewModel.startCamera(viewLifecycleOwner, requireContext(), binding.previewView, ::onResult)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        vibrator.cancel()
    }

    private fun onResult(state: ScannerViewState, result: String?) {
        when(state)
        {
            ScannerViewState.Success -> {
                vibrateOnScan()

                val url = (result ?: "").toUri()
                Log.d("ScannerFragment", "QR Code Result: $url, isHierarchical: ${url.isHierarchical}, scheme: ${url.scheme}, host: ${url.host}")

                if (url.isHierarchical && url.scheme?.trim() == "purrytify" && url.host?.trim() == "song") {
                    findNavController().navigate(url)
                } else {
                    Toast.makeText(requireContext(), "No valid URL found in QR code", Toast.LENGTH_SHORT).show()
                }


            }
            ScannerViewState.Error -> {
                Toast.makeText(requireContext(), "error =${result}", Toast.LENGTH_SHORT).show()
            }
            else -> {
                Toast.makeText(requireContext(), "error =${result}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateAnimator(transit: Int, enter: Boolean, nextAnim: Int): Animator? {
        super.onCreateAnimation(transit, enter, nextAnim)
        if (nextAnim == 0) {
            val animator = AnimatorInflater.loadAnimator(requireContext(), R.animator.barcode_scanner_animator)

            return animator
        }

        return null
    }
}