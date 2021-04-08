package com.danapps.letstalk

import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.danapps.letstalk.adapters.ChatAdapter
import com.danapps.letstalk.models.ChatMessage
import com.danapps.letstalk.models.Contact
import com.danapps.letstalk.viewmodel.LetsTalkViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import io.socket.client.Socket
import kotlinx.android.synthetic.main.activity_chat.*
import java.util.*

class ChatActivity : AppCompatActivity() {
    private lateinit var letsTalkViewModel: LetsTalkViewModel
    private lateinit var mSocket: Socket
    lateinit var myNumber: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val theme = findViewById<ConstraintLayout>(R.id.chat_main_layout)
        val animationDrawable = theme.background as AnimationDrawable
        animationDrawable.setEnterFadeDuration(250)
        animationDrawable.setExitFadeDuration(250)
        animationDrawable.start()

        letsTalkViewModel =
            ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application)).get(
                LetsTalkViewModel::class.java
            )

        val contact = Gson().fromJson(intent.extras?.get("contact").toString(), Contact::class.java)
        myNumber = FirebaseAuth.getInstance().currentUser!!.phoneNumber!!.substring(3)
        mSocket = (application as SocketInstance).mSocket


        val adapter = ChatAdapter(myNumber)
        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true
        messagesList.layoutManager = layoutManager
        messagesList.adapter = adapter

        letsTalkViewModel.getChats(myNumber, contact.number).observe(this, {
            adapter.submitList(it)
            messagesList.smoothScrollToPosition(adapter.itemCount)
        })

        sendMessage.setOnClickListener {
            val msg = message.text.toString().trim()
            if (!TextUtils.isEmpty(msg)) {
                val sendMsg = Gson().toJson(SendMsg(contact.number, msg))
                val chatMessage = ChatMessage(
                    from = myNumber,
                    to = contact.number,
                    msg = msg,
                    timeStamp = Date()
                )
                letsTalkViewModel.insertChat(chatMessage)
                mSocket.emit("message", sendMsg)
                message.text.clear()
            } else {
                Toast.makeText(this, "Please Enter The Message", Toast.LENGTH_SHORT).show()

            }
        }





        mSocket.on("message") {

            val getMsg = Gson().fromJson(it[0].toString(), SendMsg::class.java)
            val chatMessage = ChatMessage(
                from = contact.number,
                to = myNumber,
                msg = getMsg.msg,
                timeStamp = Date()
            )
            letsTalkViewModel.insertChat(chatMessage)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mSocket.off("message")
    }

    data class SendMsg(val who: String, val msg: String)
}