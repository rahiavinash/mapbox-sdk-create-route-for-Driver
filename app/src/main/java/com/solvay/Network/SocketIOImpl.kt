package com.solvay.Network

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.cli.Options
import com.solvay.BuildConfig
import com.solvay.Utils.Prefs

import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import io.socket.engineio.client.transports.Polling
import io.socket.engineio.client.transports.WebSocket
import okhttp3.OkHttpClient
import org.json.JSONException
import java.net.URI
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
//private var socketURl = "http://192.168.3.117:9999"  //Local
private var socketURl = "https://api.trucktracker.io"  //Local

val socketUrlWithToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJfaWQiOiI2N2Y0ZDk1OTI3Nzc4NDA0NjdiZjlkMzIiLCJwbGF0ZU51bWJlciI6InNrZGpmMiIsImRldmljZVRva2VuIjoibHNka2pmIiwiaWF0IjoxNzQ0MDk5NjczLCJleHAiOjE3NDQ3MDQ0NzN9.Tx4YcBDe2EqBv2Skw8YYmA05nVGOurp-qVxmNisY_BY"

class SocketIOImpl : SocketIO {
    private var socket: Socket? = null

    private var isConnecting = false

    @Synchronized
    override fun connect() {
        Log.d("SocketObserver_1","CallProcess->Connect Socket")

        try {
            socket = IO.socket(URI(BuildConfig.BASE_URL),
                IO.Options().apply {
                    secure = true
                    reconnection = true
                    timeout = 5000
                    extraHeaders = mapOf("Authorization" to listOf("Bearer " + Prefs.init().accessToken))
                    transports = listOf(
                        Polling.NAME,
                        WebSocket.NAME
                    ).toTypedArray()
                    println("CallProcess->SocketToken -> UserId->${Prefs.init().profileId}")
                })
        } catch (e: Exception) {
            e.printStackTrace()
        }

        setupSocketListeners()

        socket?.connect()

        socket?.on(Socket.EVENT_CONNECT) {
            println("Socket connected!")
        }
//
//        try {
//            socket = IO.socket(URI(socketURl),
//                IO.Options().apply {
//                    secure = true
//                    reconnection = true
//                    timeout = 5000
//                    extraHeaders =
//                        mapOf("authorization" to listOf("Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJfaWQiOiI2N2Y0ZDk1OTI3Nzc4NDA0NjdiZjlkMzIiLCJwbGF0ZU51bWJlciI6InNrZGpmMiIsImRldmljZVRva2VuIjoibHNka2pmIiwiaWF0IjoxNzQ0MDk5NjczLCJleHAiOjE3NDQ3MDQ0NzN9.Tx4YcBDe2EqBv2Skw8YYmA05nVGOurp-qVxmNisY_BY"))
//                    transports =
//                        listOf(
//                            Polling.NAME,
//                            io.socket.engineio.client.transports.WebSocket.NAME
//                        ).toTypedArray()
//                    println("CallProcess->SocketToken -> UserId->${Prefs.init().profileId}")
//
//                })
//
//        } catch (e: Exception) {
////            Log.e("SocketERROR", "dfk${e.message}")
//            e.printStackTrace()
//        }
//        setupSocketListeners()
//        socket?.connect()
    }


    @Synchronized
    override fun isConnected(): Boolean {
        return if (socket?.connected() != null) {
            socket?.connected()!!
        } else {
            false
        }
    }

    @Synchronized
    override fun disconnect() {
        Log.d("SocketObserver_1","Requesting socket disconnection from SocketIO disconnect()")
        socket?.disconnect()
        socket?.close()
        socket = null
    }

    @Synchronized
    override fun listenOn(key: String, value: Emitter.Listener) {
        socket?.on(key, value)
    }

    @Synchronized
    override fun listenOff(key: String) {
        socket?.off(key)
    }

    @Synchronized
    override fun <T> emitMessage(key: String, value: T, onResult: (key: String, value: T) -> Unit) {
        if (isConnected()) {
            try {
                socket?.emit(key, value)
                println("CallProcess->EmitMessage ${key} ${socket?.connected()}")
            } catch (e: JSONException) {
                println("CallProcess->EmitMessage${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun connectSocketAndListenForEvents(
        onConnectionChanged: (isConnected: Boolean) -> Unit
    ) {
        if (socket?.connected() == true) {
            Log.d("SocketObserver_1", "Socket is already connected. Denying new connection request.")
            return // Deny new connection requests if already connected
        }
        // Only attempt to connect if the socket is not already connected and not in the process of connecting
        if (socket == null || !socket!!.connected()) {
            socket?.connect() // Connect to the socket if not already connected
        }

        // Listen for connection success
        socket?.on(Socket.EVENT_CONNECT) {
            Handler(Looper.getMainLooper()).post {
                Log.d("SocketObserver_1", "Socket Connected - connectSocketAndListenForEvents()")
                isConnecting = false // Reset connecting flag
                onConnectionChanged(true) // Update connection status to connected
            }
        }

        // Listen for disconnection
        socket?.on(Socket.EVENT_DISCONNECT) {
            Handler(Looper.getMainLooper()).post {
                Log.d("SocketObserver_1", "Socket Disconnected - connectSocketAndListenForEvents()")
                isConnecting = false // Reset connecting flag
                onConnectionChanged(false) // Update connection status to disconnected
            }
        }

        // Listen for connection errors
        socket?.on(Socket.EVENT_CONNECT_ERROR) { args ->
            Handler(Looper.getMainLooper()).post {
                Log.e("SocketObserver_1", "Socket Connect Error: ${args[0]}\nconnectSocketAndListenForEvents()")
                isConnecting = false // Reset connecting flag on error
                onConnectionChanged(false) // Mark as disconnected on error
            }
        }
        // Handle incoming messages
        socket?.on("message") { args ->
            try {
                val message = args[0] as String
                Log.d("SocketObserver_1", "Received message: $message")
                // Handle message here
            } catch (e: Exception) {
                Log.e("SocketObserver", "Error processing message: ${e.message}")
            }
        }
    }


    private fun setupSocketListeners() {
        listenOn(
            Socket.EVENT_CONNECT
        ) { args ->
            Handler(Looper.getMainLooper()).post {
                Log.d("EVENApiT CONNECTED", isConnected().toString())

            }
        }
        listenOn(
            io.socket.client.Socket.EVENT_DISCONNECT
        ) { args ->


            Handler(Looper.getMainLooper()).post {
                Log.d("EVENT CONNECT DISCONNECT", args[0].toString())
            }
        }


        listenOn(
            io.socket.client.Socket.EVENT_CONNECT_ERROR
        ) {

                args ->
            Log.d("EVENT CONNECT ERROR", args[0].toString())

            Handler(Looper.getMainLooper()).post {

            }
        }


    }

}
