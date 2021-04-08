package com.danapps.letstalk

import android.Manifest
import android.app.ActivityOptions
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.danapps.letstalk.adapters.FragmentAdapter
import com.danapps.letstalk.adapters.NewChatAdapter
import com.danapps.letstalk.fragments.CameraFragment
import com.danapps.letstalk.fragments.ChatsFragment
import com.danapps.letstalk.models.Contact
import com.danapps.letstalk.viewmodel.LetsTalkViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import io.socket.client.Socket
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    //    private val TAG = "TEST"
    private lateinit var mAuth: FirebaseAuth
    private lateinit var letsTalkViewModel: LetsTalkViewModel
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private val newChatAdapter = NewChatAdapter()
    private var contactsSet = false
    private lateinit var mSocket: Socket

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mAuth = FirebaseAuth.getInstance()
        setSupportActionBar(toolbarMain)

        (application as SocketInstance).connectSocket(mAuth.currentUser!!.phoneNumber!!.substring(3))
        mSocket = (application as SocketInstance).mSocket
        mSocket.connect()

        letsTalkViewModel = ViewModelProvider(
            this, ViewModelProvider.AndroidViewModelFactory.getInstance(
                application
            )
        ).get(
            LetsTalkViewModel::class.java
        )

        newChatList.layoutManager = LinearLayoutManager(this)
        newChatList.adapter = newChatAdapter

        newChatAdapter.setNewChatClickListener(object : NewChatAdapter.NewChatClickListener {
            override fun onClick(contact: Contact) {
                val intent = Intent(this@MainActivity, ChatActivity::class.java)
                intent.putExtra(
                    "contact",
                    Gson().toJson(contact)
                )
                startActivity(intent)
            }

        })

        viewPager.adapter = FragmentAdapter(
            arrayListOf(CameraFragment(), ChatsFragment()),
            this.supportFragmentManager,
            this.lifecycle
        )
        viewPager.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View?) {
                viewPager.currentItem = 1
            }

            override fun onViewDetachedFromWindow(v: View?) {

            }

        })
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position == 0) {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                    supportActionBar?.hide()
                    tablayoutMain.visibility = View.GONE
                    startChat.hide()
                } else {
                    supportActionBar?.show()
                    tablayoutMain.visibility = View.VISIBLE
                    startChat.show()
                }
            }
        })
        TabLayoutMediator(tablayoutMain, viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "Camera"
                1 -> tab.text = "Chats"
            }
        }.attach()

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        startChat.tag = "1"
                        startChat.setImageDrawable(
                            ContextCompat.getDrawable(
                                this@MainActivity,
                                R.drawable.ic_close
                            )
                        )
                    }
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        startChat.tag = "0"
                        startChat.setImageDrawable(
                            ContextCompat.getDrawable(
                                this@MainActivity,
                                R.drawable.ic_message
                            )
                        )
                    }
                    BottomSheetBehavior.STATE_DRAGGING -> {
                    }
                    BottomSheetBehavior.STATE_EXPANDED -> {
                    }
                    BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                    }
                    BottomSheetBehavior.STATE_SETTLING -> {
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {

            }

        })
        startChat.setOnClickListener {
            when (it.tag) {
                "0" -> {
                    if (!contactsSet) {
                        when {
                            ContextCompat.checkSelfPermission(
                                this,
                                Manifest.permission.READ_CONTACTS
                            ) == PackageManager.PERMISSION_GRANTED -> {
                                letsTalkViewModel.syncedContactsLive.observe(this, { contactsList ->
                                    newChatAdapter.submitList(contactsList)
                                })
                                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                                contactsSet = true
                            }
                            ActivityCompat.shouldShowRequestPermissionRationale(
                                this,
                                Manifest.permission.READ_CONTACTS
                            ) -> {
                                AlertDialog.Builder(this)
                                    .setTitle("Provide Contacts Permission...")
                                    .setMessage("Contacts Permission Is Required By LetsTalk To Show Your Contacts On The App")
                                    .setPositiveButton("GRANT") { dialog, _ ->
                                        dialog.dismiss()
                                        requestPermissions(
                                            arrayOf(Manifest.permission.READ_CONTACTS),
                                            121
                                        )
                                    }
                                    .setNegativeButton("DENY") { dialog, _ ->
                                        dialog.dismiss()
                                    }
                                    .create()
                                    .show()
                            }
                            else -> ActivityCompat.requestPermissions(
                                this,
                                arrayOf(Manifest.permission.READ_CONTACTS),
                                121
                            )
                        }

                    } else
                        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                }
                "1" -> {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.seeProfile -> {
                startActivity(
                    Intent(this, ProfileActivity::class.java),
                    ActivityOptions.makeSceneTransitionAnimation(this).toBundle()
                )
            }
            R.id.refreshContacts -> {
                when {
                    ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_CONTACTS
                    ) == PackageManager.PERMISSION_GRANTED -> {
                        letsTalkViewModel.syncContacts()
                    }
                    ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.READ_CONTACTS
                    ) -> {
                        AlertDialog.Builder(this)
                            .setTitle("Provide Contacts Permission...")
                            .setMessage("Contacts Permission Is Required By LetsTalk To Show Your Contacts On The App")
                            .setPositiveButton("GRANT") { dialog, _ ->
                                dialog.dismiss()
                                requestPermissions(
                                    arrayOf(Manifest.permission.READ_CONTACTS),
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
                            arrayOf(Manifest.permission.READ_CONTACTS),
                            121
                        )
                    }
                }
            }
            R.id.logOut -> {
                mAuth.signOut()
                letsTalkViewModel.deleteUser()
                startActivity(Intent(this, InitActivity::class.java))
                finish()
            }
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 121 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            letsTalkViewModel.syncContacts()
            letsTalkViewModel.syncedContactsLive.observe(this, { contactsList ->
                newChatAdapter.submitList(contactsList)
            })
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            contactsSet = true
        }
    }

    override fun onBackPressed() {
        when {
            bottomSheetBehavior.state != BottomSheetBehavior.STATE_HIDDEN -> {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            }
            viewPager.currentItem != 1 -> {
                viewPager.currentItem = 1
            }
            else -> super.onBackPressed()
        }
    }

    override fun onDestroy() {
        mSocket.disconnect()
        super.onDestroy()
    }
}