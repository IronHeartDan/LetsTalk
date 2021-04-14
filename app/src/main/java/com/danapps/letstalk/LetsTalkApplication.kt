package com.danapps.letstalk

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.util.Log
import android.widget.Toast
import com.danapps.letstalk.Constants.Companion.BASE_URL
import com.danapps.letstalk.data.Dao
import com.danapps.letstalk.data.LetsTalkDatabase
import com.danapps.letstalk.models.User
import com.google.firebase.auth.FirebaseAuth
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class LetsTalkApplication : Application() {
    lateinit var mSocket: Socket
    lateinit var database: LetsTalkDatabase
    lateinit var dao: Dao
    var user: User? = null

    override fun onCreate() {
        super.onCreate()

        database = LetsTalkDatabase.getDatabase(this)
        dao = database.dao()

        if (FirebaseAuth.getInstance().currentUser != null) {
            val number = FirebaseAuth.getInstance().currentUser!!.phoneNumber!!.substring(3)
            GlobalScope.launch {
                if (dao.userExists(number)) {
                    connectSocket(number, false)
                }
            }
        }

        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("121212", name, importance).apply {
                description = descriptionText
                enableLights(true)
                lightColor = Color.CYAN
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun connectSocket(number: String, showOnline: Boolean) {
        try {
            mSocket = IO.socket("$BASE_URL/?number=$number&online=$showOnline")
            mSocket.connect()
        } catch (e: Exception) {
            Log.d("LetsTalkApplication", "Failed to connect ${e.message}")
            Toast.makeText(applicationContext, "Failed to connect ${e.message}", Toast.LENGTH_SHORT)
                .show()
        }
    }

    fun isSocketInitialized() = ::mSocket.isInitialized
}