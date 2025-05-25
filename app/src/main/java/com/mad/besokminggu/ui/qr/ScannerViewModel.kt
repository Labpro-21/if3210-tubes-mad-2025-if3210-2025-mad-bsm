package com.mad.besokminggu.ui.qr

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.mad.besokminggu.manager.ScannerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class ScannerViewModel @Inject constructor(): ViewModel(){
    private lateinit var qrCodeManager: ScannerManager

    /**
     * Initialize Camera Manager class.
     */
    internal fun startCamera(
        viewLifecycleOwner: LifecycleOwner,
        context: Context,
        previewView: PreviewView,
        onResult: (state: ScannerViewState, result: String) -> Unit,
    ) {
        qrCodeManager = ScannerManager(
            owner = viewLifecycleOwner, context = context,
            viewPreview = previewView,
            onResult = onResult,
            lensFacing = CameraSelector.LENS_FACING_BACK
        )
    }
}