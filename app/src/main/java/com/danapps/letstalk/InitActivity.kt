package com.danapps.letstalk

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.danapps.letstalk.models.User
import com.danapps.letstalk.viewmodel.LetsTalkViewModel


class InitActivity : AppCompatActivity() {
    lateinit var initNumber: String
    lateinit var initName: String
    var initProfile: String? = null
    private lateinit var letsTalkViewModel: LetsTalkViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_init)
        letsTalkViewModel = ViewModelProvider(
            this, ViewModelProvider.AndroidViewModelFactory.getInstance(
                application
            )
        ).get(
            LetsTalkViewModel::class.java
        )
    }

    fun initUser() {
        val user = User(initName,initNumber,initProfile)
        letsTalkViewModel.getUser(user,this)
    }

    fun initalizedUser() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

}