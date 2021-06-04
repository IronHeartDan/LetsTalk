package com.danapps.letstalk

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import com.danapps.letstalk.Constants.Companion.BASE_URL
import com.danapps.letstalk.activities.MessageActivity
import com.danapps.letstalk.activities.RtcActivity
import com.danapps.letstalk.data.Dao
import com.danapps.letstalk.data.LetsTalkDatabase
import com.danapps.letstalk.models.ChatMessage
import com.danapps.letstalk.models.RtcCall
import com.danapps.letstalk.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
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
        val theme = ThemeProvider(this).getThemeFromPreferences()
        AppCompatDelegate.setDefaultNightMode(theme)

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
            //Create Message Notification Channel
            var name = getString(R.string.message_channel_name)
            var descriptionText = getString(R.string.message_channel_description)
            var importance = NotificationManager.IMPORTANCE_HIGH
            var channel = NotificationChannel("121212", name, importance).apply {
                description = descriptionText
                enableLights(true)
                lightColor = Color.CYAN
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)


            //Create RTC Notification Channel
            name = getString(R.string.rtc_channel_name)
            descriptionText = getString(R.string.rtc_channel_description)
            importance = NotificationManager.IMPORTANCE_HIGH
            channel = NotificationChannel("212121", name, importance).apply {
                description = descriptionText

            }
            // Register the channel with the system
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun connectSocket(number: String, showOnline: Boolean) {
        try {
            mSocket = IO.socket("$BASE_URL/?number=$number&online=$showOnline")
            mSocket.connect()
            mSocket.on(Socket.EVENT_CONNECT) {
                startListening()
            }
        } catch (e: Exception) {
            Log.d("LetsTalkApplication", "Failed to connect ${e.message}")
            Toast.makeText(applicationContext, "Failed to connect ${e.message}", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun startListening() {
        //Chat Insert

        mSocket.on("message") {
            val msgParcel = Gson().fromJson(it[0].toString(), ChatMessage::class.java)
            val conId =
                ((msgParcel.from.toLong() / 725760) + (msgParcel.to.toLong() / 725760)).toString()
            val chatMessage = ChatMessage(
                conId = conId,
                from = msgParcel.from,
                to = msgParcel.to,
                msg = msgParcel.msg,
                null
            )
            GlobalScope.launch {
                dao.insertChat(chatMessage)
            }
        }


        mSocket.on("msgStats") {
            val msgParcel = Gson().fromJson(it[0].toString(), ChatMessage::class.java)
            val conId =
                ((msgParcel.from.toLong() / 725760) + (msgParcel.to.toLong() / 725760)).toString()
            msgParcel.conId = conId
            GlobalScope.launch {
                dao.updateChat(msgParcel)
            }
        }



        mSocket.on("markSeen") {
            val markSeen = Gson().fromJson(it[0].toString(), MessageActivity.MarkSeen::class.java)
            GlobalScope.launch {
                dao.markSeen(markSeen.to, markSeen.from)
            }
        }

        mSocket.on("rtcCall") {
            val rtcCall = Gson().fromJson(it[0].toString(), RtcCall::class.java)
            Log.d("LetsTalkApplication", "startListening: ${rtcCall.from} is Calling")

            startActivity(Intent(this, RtcActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                putExtra("offer",rtcCall.offer)
                putExtra("from", rtcCall.from)
                putExtra("to",rtcCall.to)
                putExtra("type",1)
            })

        }
    }

    fun isSocketInitialized() = ::mSocket.isInitialized
}