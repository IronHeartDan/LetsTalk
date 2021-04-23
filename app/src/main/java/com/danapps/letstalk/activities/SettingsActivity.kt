package com.danapps.letstalk.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.danapps.letstalk.R
import com.danapps.letstalk.fragments.SettingsFragment
import kotlinx.android.synthetic.main.settings_activity.*

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        setSupportActionBar(settingsToolbar)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settingsFrame, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
}