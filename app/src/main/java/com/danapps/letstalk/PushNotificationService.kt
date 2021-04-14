package com.danapps.letstalk

import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.danapps.letstalk.models.ChatMessage
import com.danapps.letstalk.models.Contact
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch


class PushNotificationService : FirebaseMessagingService() {
    override fun onNewToken(p0: String) {
        Log.d("LetsTalkApplication", "onNewToken: $p0")
        super.onNewToken(p0)
    }


    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d("LetsTalkApplication", "onMessageReceived: ${remoteMessage.data["body"]}")
        val msg = Gson().fromJson(remoteMessage.data["body"], ChatMessage::class.java)
        var contact: Contact? = null
        val application = (application as LetsTalkApplication)
        val dao = application.dao
        GlobalScope.launch {
            if (dao.contactExists(msg.from)) {
                contact = async { dao.getContact(msg.from)[0] }.await()
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

    fun setNoti(chatMessage: ChatMessage, contact: Contact) {

        val id = ((chatMessage.from.toLong() / 725760) + (chatMessage.to.toLong() / 725760)).toInt()

        // Create an explicit intent for an Activity in your app
        val intent = Intent(this, ChatActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        intent.putExtra(
            "contact",
            Gson().toJson(contact)
        )
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        val builder = NotificationCompat.Builder(this, "121212")
            .setContentTitle(contact.name)
            .setContentText(chatMessage.msg)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(chatMessage.msg)
            )
            .setSmallIcon(R.drawable.ic_message)
            // Set the intent that will fire when the user taps the notification
//            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)

        with(NotificationManagerCompat.from(this)) {
            // notificationId is a unique int for each notification that you must define
            notify(id, builder.build())
        }
    }
}