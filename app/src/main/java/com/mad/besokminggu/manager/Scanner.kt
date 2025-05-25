package com.mad.besokminggu.manager

import android.content.Context
import android.util.Log
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * This class is the base class to handle all the camera functionality .
 */
abstract class BaseCameraManager(
    private val owner: LifecycleOwner,
    private val context: Context,
    private val viewPreview: PreviewView,
    private var lensFacing: Int,
    private val showHideFlashIcon: (show: Int) -> Unit
) : DefaultLifecycleObserver {
    private var imgCapture: ImageCapture? = null
    private lateinit var cameraProvider: ProcessCameraProvider
    private var stopped: Boolean = false
    protected var camera: Camera?= null
    private var flashMode: Int = ImageCapture.FLASH_MODE_OFF

    protected val cameraExecutor: ExecutorService by lazy {
        Executors.newSingleThreadExecutor()
    }

    init {
        owner.lifecycle.addObserver(this)
        startCamera()
    }

    private fun startCamera(isSwitchButtonClicked: Boolean = false) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            controlWhichCameraToDisplay(isSwitchButtonClicked)
            bindCameraUseCases()
        }, ContextCompat.getMainExecutor(context))
    }

    private fun controlWhichCameraToDisplay(isSwitchButtonClicked: Boolean): Int {
        if (isSwitchButtonClicked) {
            lensFacing =
                if (lensFacing == CameraSelector.LENS_FACING_FRONT) CameraSelector.LENS_FACING_BACK
                else CameraSelector.LENS_FACING_FRONT
        } else lensFacing
        showHideFlashIcon(lensFacing)
        return lensFacing
    }

    private fun bindCameraUseCases() {
        val cameraSelector = getCameraSelector()
        val previewView = getPreviewUseCase()
        imgCapture = getImageCapture()
        cameraProvider.unbindAll()
        try {
            imgCapture?.let {
                bindToLifecycle(cameraProvider, owner, cameraSelector, previewView, it)
            }
            previewView.setSurfaceProvider(viewPreview.surfaceProvider)
        } catch (exc: Exception) {
            Log.e("BaseCameraManager", "Use case binding failed $exc")
        }
    }
    override fun onPause(owner: LifecycleOwner) {
        if (this::cameraProvider.isInitialized) {
            cameraProvider.unbindAll()
            stopped = true
            super.onPause(owner)
        }
    }

    override fun onResume(owner: LifecycleOwner) {
        if (stopped) {
            bindCameraUseCases()
            stopped = false
        }
        super.onResume(owner)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        cameraExecutor.shutdown()
    }

    protected abstract fun bindToLifecycle(
        cameraProvider: ProcessCameraProvider,
        owner: LifecycleOwner,
        cameraSelector: CameraSelector,
        previewView: Preview,
        imageCapture: ImageCapture
    )

    private fun getCameraSelector(): CameraSelector = CameraSelector.Builder()
        .requireLensFacing(lensFacing)
        .build()

    private fun getPreviewUseCase(): Preview = Preview.Builder()
        .build()

    private fun getImageCapture(): ImageCapture = ImageCapture.Builder().setFlashMode(flashMode).build()
}