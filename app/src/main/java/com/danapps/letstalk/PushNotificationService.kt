package com.danapps.letstalk

import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.danapps.letstalk.activities.ChatActivity
import com.danapps.letstalk.activities.MainActivity
import com.danapps.letstalk.models.ChatMessage
import com.danapps.letstalk.models.Contact
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import kotlinx.coroutines.*


class PushNotificationService : FirebaseMessagingService() {
    private lateinit var application: LetsTalkApplication
    override fun onNewToken(p0: String) {
        Log.d("LetsTalkApplication", "onNewToken: $p0")
        super.onNewToken(p0)
    }


    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        application = (getApplication() as LetsTalkApplication)
        Log.d("LetsTalkApplication", "onMessageReceived: ${remoteMessage.data}")

        when (remoteMessage.data["data_type"]) {
            "1" -> {
                initNotification(remoteMessage.data["msg"])
            }
            "2" -> {
                Log.d("LetsTalkApplication", "onMessageReceived: 2")
                val msgParcel =
                    Gson().fromJson(remoteMessage.data["data_sent"], ChatMessage::class.java)
                GlobalScope.launch {
                    application.dao.updateChat(msgParcel)
                }

            }
            "3" -> {
                Log.d("LetsTalkApplication", "onMessageReceived: 3")
                Log.d("LetsTalkApplication", "onMessageReceived: ${remoteMessage.data["data_seen"]}")
                val markSeen = Gson().fromJson(
                    remoteMessage.data["data_seen"],
                    ChatActivity.MarkSeen::class.java
                )
                GlobalScope.launch {
                    application.dao.markSeen(markSeen.to, markSeen.from)
                }
            }
        }
    }

    private fun initNotification(s: String?) {
        val msg = Gson().fromJson(s, ChatMessage::class.java)
        var contact: Contact?
        val dao = application.dao
        GlobalScope.launch {
            if (dao.contactExists(msg.from)) {
                contact = withContext(Dispatchers.Default) {
                    dao.getContact(msg.from)[0]
                }
                setNoti(msg, contact!!)
            } else {
                contact = Contact(msg.from, null, msg.from)
                setNoti(msg, contact!!)
            }
        }


        // Save And Acknowledgement
        val chatMessage = ChatMessage(
            msg.from,
            msg.to,
            msg.msg,
            null
        )

        GlobalScope.launch {
            val await = async {
                dao.insertChat(chatMessage)
            }
            await.await()
            msg.msgStats = 2
            val sendMsg =
                Gson().toJson(msg)
            try {
                application.mSocket.emit("msgStats", sendMsg)
            } catch (e: Exception) {
                Log.d("LetsTalkApplication", "Exception: ${e.message}")
            }
        }
    }

    private fun setNoti(chatMessage: ChatMessage, contact: Contact) {
        Log.d("LetsTalkApplication", "setNoti:")
        val id = ((chatMessage.from.toLong() / 725760) + (chatMessage.to.toLong() / 725760)).toInt()

        val chatIntent = Intent(this, ChatActivity::class.java)
        val mainIntent = Intent(this, MainActivity::class.java)

        chatIntent.putExtra(
            "contact",
            Gson().toJson(contact)
        )

        val pendingIntent = PendingIntent.getActivity(
            this, 0, chatIntent, PendingIntent.FLAG_ONE_SHOT
        )

        val builder = NotificationCompat.Builder(this, "121212")
            .setContentTitle(contact.name)
            .setContentText(chatMessage.msg)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(chatMessage.msg)
            )
            .setSmallIcon(R.drawable.ic_message)
            // Set the intent that will fire when the user taps the notification
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)



        with(NotificationManagerCompat.from(this)) {
            // notificationId is a unique int for each notification that you must define
            notify(id, builder.build())
        }
    }
}