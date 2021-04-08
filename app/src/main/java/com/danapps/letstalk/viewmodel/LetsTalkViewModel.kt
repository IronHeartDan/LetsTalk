package com.danapps.letstalk.viewmodel

import android.app.Application
import android.content.ContentResolver
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.danapps.letstalk.InitActivity
import com.danapps.letstalk.SplashActivity
import com.danapps.letstalk.contentproviders.MediaLiveData
import com.danapps.letstalk.data.Dao
import com.danapps.letstalk.data.LetsTalkDatabase
import com.danapps.letstalk.data.RetroFitBuilder
import com.danapps.letstalk.models.ChatMessage
import com.danapps.letstalk.models.Contact
import com.danapps.letstalk.models.User
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LetsTalkViewModel(private val viewModelApplication: Application) :
    AndroidViewModel(viewModelApplication) {
    val mediaLive = MediaLiveData(viewModelApplication.applicationContext)
    private lateinit var database: LetsTalkDatabase
    private lateinit var dao: Dao
    lateinit var syncedContactsLive: LiveData<List<Contact>>

    init {
        viewModelScope.launch {
            database = LetsTalkDatabase.getDatabase(viewModelApplication.applicationContext)
            dao = database.dao()
            syncedContactsLive = dao.getSyncedContacts()
        }
    }


    //Chat System

    fun insertChat(chatMessage: ChatMessage) {
        viewModelScope.launch {
            dao.insertChat(chatMessage)
        }
    }

    fun getChats(from: String, to: String): LiveData<List<ChatMessage>> {
        return dao.getChats(from, to)
    }


    fun syncContacts() {
        val contacts = mutableListOf<Contact>()
        val cursor: Cursor?
        val normalizedNumbers: HashSet<String> = HashSet()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            cursor = viewModelApplication.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(
                    ContactsContract.Contacts.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER
                ),
                Bundle().apply
                {
                    // Sort function
                    putStringArray(
                        ContentResolver.QUERY_ARG_SORT_COLUMNS,
                        arrayOf(ContactsContract.Contacts.DISPLAY_NAME)
                    )
                }, null
            )
        } else {
            cursor = viewModelApplication.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(
                    ContactsContract.Contacts.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER
                ),
                null,
                null,
                ContactsContract.Contacts.DISPLAY_NAME + "ASC"
            )
        }
        while (cursor!!.moveToNext()) {
            val name =
                cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
            var number =
                cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
            if (number.length > 10) {
                if (number.startsWith("+")) {
                    if (number[3] == ' ') {
                        number = number.substring(4)
                    } else {
                        number = number.substring(3)
                    }
                } else if (number.startsWith("0")) {
                    number = number.substring(1)
                }
            }
            number = number.replace(" ", "")
            val normalizedNum =
                cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER))
            if (normalizedNumbers.add(normalizedNum)) {
                contacts.add(Contact(name, null, number))
            }
        }
        cursor.close()

        contacts.forEach {
            RetroFitBuilder.apiService.userExists(it.number).enqueue(object : Callback<Boolean> {
                override fun onResponse(call: Call<Boolean>, response: Response<Boolean>) {
                    if (response.code() == 200) {
                        viewModelScope.launch {
                            if (response.body() == true) {
                                if (!dao.checkSyncedContact(it.number))
                                    dao.insertSyncedContact(it)
                            } else {
                                if (dao.checkSyncedContact(it.number))
                                    dao.deleteSyncedContact(it)
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<Boolean>, t: Throwable) {
                    Log.d("TEST", "onFailure: ${t.message}")
                }

            })
        }
    }


    fun exists(number: String, activity: SplashActivity) {
        viewModelScope.launch {
            activity.letsGo(dao.getUser(number))
        }
    }

    fun deleteUser() {
        viewModelScope.launch {
            dao.logOut()
        }
    }

    fun existsOrCreate(user: User, activity: InitActivity) {
        RetroFitBuilder.apiService.userExists(user.number).enqueue(object : Callback<Boolean> {
            override fun onResponse(call: Call<Boolean>, response: Response<Boolean>) {
                if (response.code() == 200) {
                    if (response.body() == true) {
                        viewModelScope.launch {
                            dao.createUser(user)
                        }
                        activity.initSyncContacts()
                    } else {
                        RetroFitBuilder.apiService.createUser(user)
                            .enqueue(object : Callback<String> {
                                override fun onResponse(
                                    call: Call<String>,
                                    response: Response<String>
                                ) {
                                    if (response.code() == 200) {
                                        if (response.body()!!.isNotEmpty()) {
                                            viewModelScope.launch {
                                                dao.createUser(user)
                                            }
                                            activity.initSyncContacts()
                                        }
                                    }
                                }

                                override fun onFailure(call: Call<String>, t: Throwable) {
                                    Log.d("TEST", "onResponse: ${t.message}")
                                }

                            })
                    }
                } else {
                    Toast.makeText(activity, response.errorBody().toString(), Toast.LENGTH_SHORT)
                        .show()
                }
            }

            override fun onFailure(call: Call<Boolean>, t: Throwable) {
                Log.d("TEST", "onFailure: ${t.message}")
            }

        })
    }
}