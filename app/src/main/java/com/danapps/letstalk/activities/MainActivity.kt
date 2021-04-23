package com.danapps.letstalk.activities

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
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
    private lateinit var navController: NavController
    private var bottomMargin: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(mainToolBarBottom)


        mAuth = FirebaseAuth.getInstance()

        number = mAuth.currentUser!!.phoneNumber!!.substring(3)

        val params = main_nav_host_fragment.layoutParams as CoordinatorLayout.LayoutParams
        bottomMargin = params.bottomMargin

        main_nav_host_fragment.addOnAttachStateChangeListener(object :
            View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View?) {
                navController = findNavController(R.id.main_nav_host_fragment)
                navController.addOnDestinationChangedListener { _, destination, _ ->
                    if (destination.id == R.id.chatsFragment) {
                        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                        showUI()
                    }
                }
            }

            override fun onViewDetachedFromWindow(v: View?) {
            }

        })


        letsTalkViewModel =
            ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application)).get(
                LetsTalkViewModel::class.java
            )


        letsTalkViewModel.liveUser(number).observe(this, {
            if (it.profile_pic.equals("null")) {
                Glide.with(this).load(R.drawable.ic_account_circle).into(mainProfile)
            } else {
                Glide.with(this).load(Uri.parse(it.profile_pic)).centerCrop().into(mainProfile)
            }

        })

        mainProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        letsTalkApplication = (application as LetsTalkApplication)

        if (!letsTalkApplication.isSocketInitialized()) {
            letsTalkApplication.connectSocket(number, true)
        }

        mSocket = (application as LetsTalkApplication).mSocket
        mSocket.emit("setOnline", Gson().toJson(SetOnline(number, true)))

        startNewChat.setOnClickListener {
            navController.navigate(R.id.action_chatsFragment_to_contactsSheet)
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.openCamera -> {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                navController.navigate(R.id.action_chatsFragment_to_cameraFragment)
                hideUI()
            }
            R.id.seeSettings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
            }
            R.id.seeStories -> {
                drawerLayout.openDrawer(GravityCompat.END)
            }
        }
        return true
    }

    private fun hideUI() {
        supportActionBar?.hide()
        mainToolBarTop.visibility = View.GONE
        mainBottomBar.performHide()
        startNewChat.hide()
        val params = main_nav_host_fragment.layoutParams as CoordinatorLayout.LayoutParams
        params.bottomMargin = 0
        main_nav_host_fragment.layoutParams = params
    }

    private fun showUI() {
        supportActionBar?.show()
        mainToolBarTop.visibility = View.VISIBLE
        mainBottomBar.performShow()
        startNewChat.show()
        val params = main_nav_host_fragment.layoutParams as CoordinatorLayout.LayoutParams
        params.bottomMargin = bottomMargin
        main_nav_host_fragment.layoutParams = params
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerVisible(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END)
        } else
            super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("LetsTalkApplication", "onDestroy: ")
        mSocket.emit("setOnline", Gson().toJson(SetOnline(number, false)))
    }
}