package com.danapps.letstalk

import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.danapps.letstalk.adapters.ChatAdapter
import com.danapps.letstalk.data.MsgParcel
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

        setSupportActionBar(chatToolBar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        letsTalkViewModel =
            ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application)).get(
                LetsTalkViewModel::class.java
            )

        val contact = Gson().fromJson(intent.extras?.get("contact").toString(), Contact::class.java)

        supportActionBar?.title = contact.name
        supportActionBar?.subtitle = contact.number


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

        var timer = Timer()
        message.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!TextUtils.isEmpty(s)) {
                    val showTyping = Gson().toJson(ShowTyping(contact.number, true))
                    mSocket.emit("typing", showTyping)
                }
                timer.cancel()
                timer = Timer()
                val delay: Long = 1000
                timer.schedule(
                    object : TimerTask() {
                        override fun run() {
                            // Typing False
                            val showTyping = Gson().toJson(ShowTyping(contact.number, false))
                            mSocket.emit("typing", showTyping)
                        }
                    },
                    delay
                )
            }

            override fun afterTextChanged(s: Editable?) {
            }

        })

        sendMessage.setOnClickListener {
            val msg = message.text.toString().trim()
            if (!TextUtils.isEmpty(msg)) {
                val sendMsg = Gson().toJson(MsgParcel(myNumber, contact.number, msg))
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
                supportActionBar?.subtitle = contact.number
                Toast.makeText(this, "Please Enter The Message", Toast.LENGTH_SHORT).show()

            }
        }

//        mSocket.on("typing") {
//            val showTyping = Gson().fromJson(it[0].toString(), ShowTyping::class.java)
//            if (showTyping.typing) {
//                supportActionBar?.subtitle = "Typing"
//            } else {
//                supportActionBar?.subtitle = "Online"
//            }
//        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }


    override fun onDestroy() {
        super.onDestroy()
        mSocket.off("message")
    }

    data class ShowTyping(val who: String, val typing: Boolean)
}