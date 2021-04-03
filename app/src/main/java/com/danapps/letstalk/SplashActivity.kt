package com.danapps.letstalk

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.danapps.letstalk.viewmodel.LetsTalkViewModel
import com.google.firebase.auth.FirebaseAuth


class SplashActivity : AppCompatActivity() {
    private lateinit var letsTalkViewModel: LetsTalkViewModel
    private lateinit var mAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        letsTalkViewModel =
            ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application)).get(
                LetsTalkViewModel::class.java
            )
        mAuth = FirebaseAuth.getInstance()

        Handler(Looper.getMainLooper()).postDelayed({
            if (mAuth.currentUser != null) {
                letsTalkViewModel.exists(mAuth.currentUser!!.phoneNumber!!.substring(3), this)
            } else {
                startActivity(Intent(this, InitActivity::class.java))
            }
            finish()
        }, 1000)
    }

    fun letsGo(exists: Boolean) {
        if (exists) {
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            startActivity(Intent(this, InitActivity::class.java))
        }
        finish()
    }
}