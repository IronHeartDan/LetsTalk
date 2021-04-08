package com.danapps.letstalk

import android.app.ActivityOptions
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.danapps.letstalk.models.User
import com.danapps.letstalk.viewmodel.LetsTalkViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_init.*


class InitActivity : AppCompatActivity() {
    lateinit var initNumber: String
    lateinit var initName: String
    var initProfile: String? = null
    private lateinit var letsTalkViewModel: LetsTalkViewModel
    private lateinit var mAuth: FirebaseAuth
    private var nav: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_init)

        mAuth = FirebaseAuth.getInstance()

        nav_host_fragment.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View?) {
                if (mAuth.currentUser != null) {
                    v?.findNavController()?.setGraph(R.navigation.cinit_navigation)
                    initNumber = mAuth.currentUser!!.phoneNumber!!.substring(3)
                    nav = 1
                } else {
                    v?.findNavController()?.setGraph(R.navigation.init_navigation)
                }
            }

            override fun onViewDetachedFromWindow(v: View?) {
            }

        })

        letsTalkViewModel = ViewModelProvider(
            this, ViewModelProvider.AndroidViewModelFactory.getInstance(
                application
            )
        ).get(
            LetsTalkViewModel::
            class.java
        )
    }

    fun initUser() {
        val user = User(initName, initNumber, initProfile)
        letsTalkViewModel.existsOrCreate(user, this)
    }

    fun initSyncContacts() {
        if (nav == 0) {
            nav_host_fragment.findNavController()
                .navigate(R.id.action_initTwoFragment_to_syncContactsFragment)
        } else {
            nav_host_fragment.findNavController()
                .navigate(R.id.action_initTwoFragment2_to_syncContactsFragment2)
        }
    }

    fun syncContacts() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED -> {
                letsTalkViewModel.syncContacts()
                initalizedUser()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                android.Manifest.permission.READ_CONTACTS
            ) -> {
                AlertDialog.Builder(this)
                    .setTitle("Provide Contacts Permission...")
                    .setMessage("Contacts Permission Is Required By LetsTalk To Show Your Contacts On The App")
                    .setPositiveButton("GRANT") { dialog, _ ->
                        dialog.dismiss()
                        requestPermissions(
                            arrayOf(android.Manifest.permission.READ_CONTACTS),
                            121
                        )
                    }
                    .setNegativeButton("DENY") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .create()
                    .show()
            }
            else -> {
                requestPermissions(
                    arrayOf(android.Manifest.permission.READ_CONTACTS),
                    121
                )
            }
        }
    }

    fun initalizedUser() {
        startActivity(Intent(this, MainActivity::class.java),
            ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
        finish()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 121 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            letsTalkViewModel.syncContacts()
            initalizedUser()
        }
    }
}