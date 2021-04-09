package com.danapps.letstalk

import android.app.Application
import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket

class SocketInstance : Application() {
    lateinit var mSocket: Socket
//    private var serverURL = "http://10.0.2.2:5000"
//    private var serverURL = "http://192.168.0.102:5000"
    private val serverURL = "https://lets-talk-backend.herokuapp.com"

    fun connectSocket(number: String) {
        try {
            mSocket = IO.socket("$serverURL/?number=$number")
        } catch (e: Exception) {
            Log.d("TEST", "Failed to connect ${e.message}")
        }
    }
}