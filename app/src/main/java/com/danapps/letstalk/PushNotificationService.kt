package com.danapps.letstalk

import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.danapps.letstalk.models.ChatMessage
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson


class PushNotificationService : FirebaseMessagingService() {
    override fun onNewToken(p0: String) {
        Log.d("LetsTalkApplication", "onNewToken: $p0")
        super.onNewToken(p0)
    }


    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d("LetsTalkApplication", "onMessageReceived: ${remoteMessage.data["body"]}")
        val msg = Gson().fromJson(remoteMessage.data["body"], ChatMessage::class.java)

        Log.d("LetsTalkApplication", "onMessageReceived: Show")

        val id = ((msg.from.toLong() / 725760) + (msg.to.toLong() / 725760)).toInt()

        // Create an explicit intent for an Activity in your app
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        val builder = NotificationCompat.Builder(this, "121212")
            .setContentTitle(msg.from)
            .setContentText(msg.msg)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(msg.msg)
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