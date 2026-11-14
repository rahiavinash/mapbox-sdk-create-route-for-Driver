package com.solvay.Network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData

class ConnectivityLiveData(private val context: Context) : LiveData<Boolean>() {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            cancelDisconnectRunnable()
            postValue(true)
        }

        override fun onLost(network: Network) {
            scheduleDisconnectRunnable()
        }
    }

    private val handler = Handler(Looper.getMainLooper())
    private val disconnectRunnable = Runnable {
        postValue(false)
    }

    private fun scheduleDisconnectRunnable() {
        handler.postDelayed(disconnectRunnable, 4000)
    }

    private fun cancelDisconnectRunnable() {
        handler.removeCallbacks(disconnectRunnable)
    }

    override fun onActive() {
        super.onActive()
        val networkRequest = NetworkRequest.Builder().build()
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }

    override fun onInactive() {
        super.onInactive()
        connectivityManager.unregisterNetworkCallback(networkCallback)
        cancelDisconnectRunnable()
    }
}
