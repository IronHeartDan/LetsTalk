package com.danapps.letstalk.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.danapps.letstalk.LetsTalkApplication
import com.danapps.letstalk.R
import com.danapps.letstalk.adapters.MessageAdapter
import com.danapps.letstalk.models.ChatMessage
import com.danapps.letstalk.models.Contact
import com.danapps.letstalk.models.SetOnline
import com.danapps.letstalk.viewmodel.LetsTalkViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import io.socket.client.Socket
import kotlinx.android.synthetic.main.activity_message.*
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.*

class MessageActivity : AppCompatActivity() {
    private lateinit var letsTalkViewModel: LetsTalkViewModel
    private lateinit var mSocket: Socket
    private lateinit var myNumber: String
    private lateinit var contact: Contact
    private var isOnline = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)


        setSupportActionBar(msgToolBar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        letsTalkViewModel =
            ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application)).get(
                LetsTalkViewModel::class.java
            )

        contact = Gson().fromJson(intent.extras?.get("contact").toString(), Contact::class.java)
        msg_title.text = contact.name

        if (contact.profile_pic != null) {
            Glide.with(this).load(Uri.parse(contact.profile_pic)).centerCrop().into(msg_acc_pic)
        } else {
            Glide.with(this).load(R.drawable.ic_account_circle).centerCrop().into(msg_acc_pic)
        }



        myNumber = FirebaseAuth.getInstance().currentUser!!.phoneNumber!!.substring(3)
        mSocket = (application as LetsTalkApplication).mSocket
        mSocket.emit("setOnline", Gson().toJson(SetOnline(myNumber, true)))
        markSeen()
        mSocket.emit("enterRoom", contact.number)
        mSocket.emit("isOnline", contact.number)
        mSocket.on("isOnline") {
            runOnUiThread {
                msg_subtitle.visibility = View.VISIBLE
                if (it[0] == true) {
                    isOnline = true
                    msg_subtitle.text = "Online"
                } else {
                    isOnline = false
                    msg_subtitle.text = "Offline"
                }
            }
        }


        val adapter = MessageAdapter(this, myNumber)
        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true
        messagesList.layoutManager = layoutManager
        messagesList.adapter = adapter

        val itemAnimator: DefaultItemAnimator = object : DefaultItemAnimator() {
            override fun canReuseUpdatedViewHolder(viewHolder: RecyclerView.ViewHolder): Boolean {
                return true
            }
        }
        messagesList.itemAnimator = itemAnimator

        letsTalkViewModel.getChats(myNumber, contact.number).observe(this, {
            adapter.submitList(it)
            messagesList.smoothScrollToPosition(adapter.itemCount)
        })

        var timer = Timer()
        message.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Log.d("TEST", "onTextChanged: $start , $before , $count")
                if (!TextUtils.isEmpty(s)) {
                    val showTyping = Gson().toJson(ShowTyping(myNumber, contact.number, true))
                    mSocket.emit("typing", showTyping)
                }
                timer.cancel()
                timer = Timer()
                val delay: Long = 1000
                timer.schedule(
                    object : TimerTask() {
                        override fun run() {
                            // Typing False
                            val showTyping =
                                Gson().toJson(ShowTyping(myNumber, contact.number, false))
                            mSocket.emit("typing", showTyping)
                        }
                    },
                    delay
                )
            }

            override fun afterTextChanged(s: Editable?) {
            }

        })

        mSocket.on("typing") {
            val showTyping = Gson().fromJson(it[0].toString(), ShowTyping::class.java)
            if (showTyping.typing) {
                msg_subtitle.visibility = View.VISIBLE
                runOnUiThread {
                    msg_subtitle.text = "Typing"
                }
            } else {
                runOnUiThread {
                    msg_subtitle.text = "Online"
                }
            }
        }

        sendMessage.setOnClickListener {
            val msg = message.text.toString().trim()
            if (!TextUtils.isEmpty(msg)) {
                val conId =
                    ((contact.number.toLong() / 725760) + (myNumber.toLong() / 725760)).toString()
                val chatMessage =
                    ChatMessage(
                        conId = conId,
                        from = myNumber,
                        to = contact.number,
                        msg = msg,
                        msgStats = 0
                    )
                letsTalkViewModel.viewModelScope.launch {
                    val id = async {
                        letsTalkViewModel.insertChat(chatMessage)
                    }
                    chatMessage.id = id.await().toInt()
                    val sendMsg =
                        Gson().toJson(chatMessage)
                    mSocket.emit("message", sendMsg)
                }
                message.text.clear()
            } else {
                Toast.makeText(this, "Please Enter The Message", Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.message_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val intent = Intent(this, RtcActivity::class.java)
        intent.putExtra("from", myNumber)
        intent.putExtra("to", contact.number)
        intent.putExtra("type",0)
        when (item.itemId) {
            R.id.chat_call -> {
                intent.putExtra("callType", 0)
                startActivity(intent)
            }
            R.id.chat_videoCall -> {
                intent.putExtra("callType", 1)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onDestroy() {
        mSocket.emit("exitRoom", contact.number)
        mSocket.off("isOnline")
        super.onDestroy()
    }


    fun markSeen() {
        mSocket.emit("markSeen", Gson().toJson(MarkSeen(myNumber, contact.number)))
    }

    data class ShowTyping(val from: String, val to: String, val typing: Boolean)
    data class MarkSeen(val from: String, val to: String)
}