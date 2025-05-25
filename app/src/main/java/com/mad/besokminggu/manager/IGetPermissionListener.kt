package com.mad.besokminggu.manager

interface IGetPermissionListener {
    fun onPermissionGranted()
    fun onPermissionRationale()
    fun onPermissionDenied()
}