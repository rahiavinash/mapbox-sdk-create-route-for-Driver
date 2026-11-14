package com.solvay.Network

import io.socket.emitter.Emitter

interface SocketIO {
    fun connect()
    fun disconnect()
    fun isConnected(): Boolean
    fun listenOn(key: String, value: Emitter.Listener)
    fun listenOff(key: String)
    fun <T> emitMessage(key: String, value: T,onResult: (key: String, value: T) -> Unit)
}