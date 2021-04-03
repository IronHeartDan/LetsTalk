package com.danapps.letstalk

import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import io.socket.client.Socket

class ChatActivity : AppCompatActivity() {
    private lateinit var mSocket: Socket
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val theme = findViewById<ConstraintLayout>(R.id.chat_main_layout)
        val animationDrawable = theme.background as AnimationDrawable
        animationDrawable.setEnterFadeDuration(250)
        animationDrawable.setExitFadeDuration(250)
        animationDrawable.start()

        mSocket = (application as SocketInstance).mSocket
        mSocket.emit("success", "Hello From ${mSocket.id()}")
    }
}