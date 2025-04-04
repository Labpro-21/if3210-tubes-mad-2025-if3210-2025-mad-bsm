package com.mad.besokminggu.network

import android.Manifest
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.annotation.RequiresPermission

/**
 * Use [enable]/[disable] to register/unregister [ConnectivityManager]
 * @property context
 * @property onNetworkAvailableCallbacks callback to notify [context] about network status changes
 */
class ConnectionStateMonitor(
    private val context: Context,
    private val onNetworkAvailableCallbacks: OnNetworkAvailableCallbacks
) : ConnectivityManager.NetworkCallback() {

    private val connectivityManager: ConnectivityManager by lazy {
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    private val networkRequest = NetworkRequest.Builder()
        .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        .build()

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    fun hasNetworkConnection(): Boolean {
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    @Throws(SecurityException::class)
    fun enable() {
        try {
            connectivityManager.registerNetworkCallback(networkRequest, this)
        } catch (e: SecurityException) {
            onNetworkAvailableCallbacks.onError("Missing NETWORK_STATE permission")
            throw e
        }
    }

    fun disable() {
        try {
            connectivityManager.unregisterNetworkCallback(this)
        } catch (e: IllegalArgumentException) {
            onNetworkAvailableCallbacks.onError("Callback was not registered")
        }
    }

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    override fun onAvailable(network: Network) {
        super.onAvailable(network)
        if (hasNetworkConnection()) {
            onNetworkAvailableCallbacks.onPositive()
        }
    }

    override fun onLost(network: Network) {
        super.onLost(network)
        onNetworkAvailableCallbacks.onNegative()
    }

    override fun onCapabilitiesChanged(
        network: Network,
        networkCapabilities: NetworkCapabilities
    ) {
        super.onCapabilitiesChanged(network, networkCapabilities)
        if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
            onNetworkAvailableCallbacks.onPositive()
        }
    }
}