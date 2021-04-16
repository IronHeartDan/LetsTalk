package com.danapps.letstalk.viewmodel

import android.app.Application
import android.content.ContentResolver
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.danapps.letstalk.LetsTalkApplication
import com.danapps.letstalk.`interface`.ContactsSyncInterface
import com.danapps.letstalk.activities.SplashActivity
import com.danapps.letstalk.contentproviders.MediaLiveData
import com.danapps.letstalk.data.Dao
import com.danapps.letstalk.data.RetroFitBuilder
import com.danapps.letstalk.models.ChatMessage
import com.danapps.letstalk.models.Chats
import com.danapps.letstalk.models.Contact
import com.danapps.letstalk.models.User
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LetsTalkViewModel(private val viewModelApplication: Application) :
    AndroidViewModel(viewModelApplication) {
    val mediaLive = MediaLiveData(viewModelApplication.applicationContext)
    private val dao: Dao = (getApplication<LetsTalkApplication>().database.dao())
    lateinit var syncedContactsLive: LiveData<List<Contact>>

    init {
        viewModelScope.launch {
            syncedContactsLive = dao.getSyncedContacts()
        }
    }


    //Chat System

    fun getChats(number: String): LiveData<List<Chats>> {
        return dao.getChats(number)
    }

    suspend fun insertChat(chatMessage: ChatMessage): Long {
        return dao.insertChat(chatMessage)
    }

    fun updateChat(chatMessage: ChatMessage) {
        viewModelScope.launch {
            dao.updateChat(chatMessage)
        }
    }

    fun markSeen(from: String, to: String) {
        viewModelScope.launch {
            dao.markSeen(from, to)
        }
    }

    fun getChats(from: String, to: String): LiveData<List<ChatMessage>> {
        return dao.getConversation(from, to)
    }


    fun syncContacts(contactsSyncInterface: ContactsSyncInterface) {
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
                ContactsContract.Contacts.DISPLAY_NAME + " ASC"
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

        Log.d("TEST", "syncContacts: ${contacts.size}")

        RetroFitBuilder.apiService.syncContacts(contacts.toTypedArray()).enqueue(object :
            Callback<Array<Contact>> {
            override fun onResponse(
                call: Call<Array<Contact>>,
                response: Response<Array<Contact>>
            ) {
                contactsSyncInterface.finished()
                Log.d("TEST", "onResponse: ${response.body()?.size}")

                viewModelScope.launch {
                    dao.deleteContacts()

                    response.body()?.forEach {
                        dao.insertSyncedContact(it)
                    }
                }

            }

            override fun onFailure(call: Call<Array<Contact>>, t: Throwable) {
                contactsSyncInterface.error(t.message)
                Log.d("TEST", "onFailure: ${t.message}")
            }

        })

    }


    fun createUser(user: User) {
        viewModelScope.launch {
            dao.createUser(user)
        }
    }

    fun updateUser(user: User) {
        viewModelScope.launch {
            dao.updateUser(user)
        }
    }

    fun exists(number: String, activity: SplashActivity) {
        viewModelScope.launch {
            activity.letsGo(dao.userExists(number))
        }
    }

    fun liveUser(number: String): LiveData<User> {
        return dao.liveUser(number)
    }

    fun deleteUser() {
        viewModelScope.launch {
            dao.logOut()
        }
    }
}