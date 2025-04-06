package com.mad.besokminggu.network

interface OnNetworkAvailableCallbacks {
    fun onPositive() // Network is available
    fun onNegative() // Network is not available
    fun onError(s: String) = Unit // Error occurred
}