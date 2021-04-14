package com.danapps.letstalk

import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.danapps.letstalk.models.ChatMessage
import com.danapps.letstalk.models.Contact
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import kotlinx.coroutines.*


class PushNotificationService : FirebaseMessagingService() {
    override fun onNewToken(p0: String) {
        Log.d("LetsTalkApplication", "onNewToken: $p0")
        super.onNewToken(p0)
    }


    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d("LetsTalkApplication", "onMessageReceived: ${remoteMessage.data["body"]}")
        val msg = Gson().fromJson(remoteMessage.data["body"], ChatMessage::class.java)
        var contact: Contact?
        val application = (application as LetsTalkApplication)
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

    fun setNoti(chatMessage: ChatMessage, contact: Contact) {

        val id = ((chatMessage.from.toLong() / 725760) + (chatMessage.to.toLong() / 725760)).toInt()

// Create an Intent for the activity you want to start
        val resultIntent = Intent(this, ChatActivity::class.java)

        resultIntent.putExtra(
            "contact",
            Gson().toJson(contact)
        )

// Create the TaskStackBuilder
        val resultPendingIntent: PendingIntent? = TaskStackBuilder.create(this).run {
            // Add the intent, which inflates the back stack
            addNextIntentWithParentStack(resultIntent)
            // Get the PendingIntent containing the entire back stack
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        }


        val builder = NotificationCompat.Builder(this, "121212")
            .setContentTitle(contact.name)
            .setContentText(chatMessage.msg)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(chatMessage.msg)
            )
            .setSmallIcon(R.drawable.ic_message)
            // Set the intent that will fire when the user taps the notification
            .setContentIntent(resultPendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)

        with(NotificationManagerCompat.from(this)) {
            // notificationId is a unique int for each notification that you must define
            notify(id, builder.build())
        }
    }
}