package com.danapps.letstalk

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import com.danapps.letstalk.data.Dao
import com.danapps.letstalk.data.LetsTalkDatabase
import com.danapps.letstalk.models.User
import io.socket.client.IO
import io.socket.client.Socket

class LetsTalkApplication : Application() {
    lateinit var mSocket: Socket
    lateinit var database: LetsTalkDatabase
    lateinit var dao: Dao
    var user: User? = null

    //    private var serverURL = "http://10.0.2.2:5000"
    private var serverURL = "http://192.168.0.102:5000"
//    private val serverURL = "https://lets-talk-backend.herokuapp.com"

    override fun onCreate() {
        super.onCreate()

        database = LetsTalkDatabase.getDatabase(this)
        dao = database.dao()

        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("121212", name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun connectSocket(number: String, showOnline: Boolean) {
        try {
            mSocket = IO.socket("$serverURL/?number=$number&online=$showOnline")
            mSocket.connect()
        } catch (e: Exception) {
            Log.d("LetsTalkApplication", "Failed to connect ${e.message}")
            Toast.makeText(applicationContext, "Failed to connect ${e.message}", Toast.LENGTH_SHORT)
                .show()
        }
    }

    override fun onTerminate() {
        if (mSocket != null) {
            if (mSocket.connected()) {
                mSocket.disconnect()
            }
        }
        super.onTerminate()
    }
}