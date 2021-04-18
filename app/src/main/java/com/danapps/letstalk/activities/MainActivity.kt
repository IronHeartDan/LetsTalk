package com.danapps.letstalk.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.danapps.letstalk.LetsTalkApplication
import com.danapps.letstalk.R
import com.danapps.letstalk.models.SetOnline
import com.danapps.letstalk.viewmodel.LetsTalkViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import io.socket.client.Socket
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mSocket: Socket
    private lateinit var number: String
    private lateinit var letsTalkApplication: LetsTalkApplication
    private lateinit var letsTalkViewModel: LetsTalkViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(mainToolBar)

        mAuth = FirebaseAuth.getInstance()

        number = mAuth.currentUser!!.phoneNumber!!.substring(3)

        letsTalkViewModel =
            ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application)).get(
                LetsTalkViewModel::class.java
            )

        letsTalkApplication = (application as LetsTalkApplication)

        if (!letsTalkApplication.isSocketInitialized()) {
            letsTalkApplication.connectSocket(number, true)
        }

        mSocket = (application as LetsTalkApplication).mSocket
        mSocket.emit("setOnline", Gson().toJson(SetOnline(number, true)))

        startNewChat.setOnClickListener {
            findNavController(R.id.main_nav_host_fragment).navigate(R.id.action_chatsFragment_to_contactsSheet)
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        val imageView = ImageView(this)
        val cardView = CardView(this)
        cardView.radius = 50F
        cardView.minimumHeight = 100
        cardView.minimumWidth = 100
        imageView.minimumHeight = 100
        imageView.minimumWidth = 100

        cardView.addView(imageView)

        imageView.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        val item: MenuItem = menu!!.getItem(0)

        letsTalkViewModel.liveUser(number).observe(this, {
            if (it.profile_pic.equals("null")) {
                Glide.with(this).load(R.drawable.ic_account_circle).into(imageView)
                item.actionView = cardView
            } else {
                Glide.with(this).load(Uri.parse(it.profile_pic)).centerCrop().into(imageView)
                item.actionView = cardView
            }

        })
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.seeSettings -> {
            }
            R.id.logOut -> {
                mAuth.signOut()
                startActivity(Intent(this, InitActivity::class.java))
                finish()
            }
        }
        return true
    }


    override fun onDestroy() {
        super.onDestroy()
        Log.d("LetsTalkApplication", "onDestroy: ")
        mSocket.emit("setOnline", Gson().toJson(SetOnline(number, false)))
    }

}