package com.mad.besokminggu.ui.qr

sealed class ScannerViewState {
    object Success : ScannerViewState()
    object Error : ScannerViewState()
}