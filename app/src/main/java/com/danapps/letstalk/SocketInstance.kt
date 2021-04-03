package com.danapps.letstalk

import android.app.Application
import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket

class SocketInstance : Application() {
    lateinit var mSocket: Socket
    private val serverURL = "http://10.0.2.2:5000"

    override fun onCreate() {
        super.onCreate()
        try {
            mSocket = IO.socket(serverURL)
            mSocket.connect()
        } catch (e: Exception) {
            Log.d("TEST", "Failed to connect ${e.message}")
        }
    }
}