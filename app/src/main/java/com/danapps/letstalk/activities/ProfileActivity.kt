package com.danapps.letstalk.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.danapps.letstalk.R
import com.danapps.letstalk.fragments.InitTwoFragment

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        supportFragmentManager.beginTransaction().add(R.id.seeProfileFrame, InitTwoFragment())
            .commit()

    }
}