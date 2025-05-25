package com.mad.besokminggu.manager

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import com.mad.besokminggu.ui.qr.ScannerAnalyzer
import com.mad.besokminggu.ui.qr.ScannerViewState

class ScannerManager(
    owner: LifecycleOwner,
    context: Context,
    viewPreview: PreviewView,
    private val onResult: (state: ScannerViewState, result: String) -> Unit,
    lensFacing: Int
) : BaseCameraManager(owner, context, viewPreview, lensFacing, {}) {

    private fun getImageAnalysis(): ImageAnalysis {
        return ImageAnalysis.Builder()
            .build()
            .also {
                it.setAnalyzer(cameraExecutor, ScannerAnalyzer(onResult))
            }
    }

    override fun bindToLifecycle(
        cameraProvider: ProcessCameraProvider,
        owner: LifecycleOwner,
        cameraSelector: CameraSelector,
        previewView: Preview,
        imageCapture: ImageCapture
    ) {
        camera = cameraProvider.bindToLifecycle(
            owner,
            cameraSelector,
            previewView,
            getImageAnalysis(),
            imageCapture
        )
    }
}