package com.solvay.Network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.util.Log

class NetworkMonitor(private val context: Context) {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    var isConnected: Boolean = false
    var onReconnect: (() -> Unit)? = null
    var onDisconnect: (() -> Unit)? = null

    fun startMonitoring() {
        val builder = NetworkRequest.Builder()

        connectivityManager.registerNetworkCallback(
            builder.build(),
            object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    if (!isConnected) {
                        isConnected = true
                        Log.d("NetworkMonitor", "Internet is back")
                        onReconnect?.invoke()
                    }
                }

                override fun onLost(network: Network) {
                    isConnected = false
                    Log.d("NetworkMonitor", "Internet lost")
                    onDisconnect?.invoke()
                }
            }
        )
    }
}
