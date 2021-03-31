package com.danapps.letstalk.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.danapps.letstalk.contentproviders.ContactsLiveData
import com.danapps.letstalk.contentproviders.MediaLiveData
import com.danapps.letstalk.data.RetroFitBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LetsTalkViewModel(application: Application) : AndroidViewModel(application) {
    val contactsLive = ContactsLiveData(application.applicationContext)
    val mediaLive = MediaLiveData(application.applicationContext)

    init {
        getUsers()
    }

    private fun getUsers() {
        RetroFitBuilder.apiService.getUsers().enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.code() == 200) {
                    Log.d("TEST", "onResponse: ${response.body()}")
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.d("TEST", "onResponse: ${t.message}")
            }

        })
    }
}