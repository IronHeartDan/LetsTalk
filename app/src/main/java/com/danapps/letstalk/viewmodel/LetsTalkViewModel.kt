package com.danapps.letstalk.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.danapps.letstalk.InitActivity
import com.danapps.letstalk.contentproviders.ContactsLiveData
import com.danapps.letstalk.contentproviders.MediaLiveData
import com.danapps.letstalk.data.RetroFitBuilder
import com.danapps.letstalk.models.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LetsTalkViewModel(application: Application) : AndroidViewModel(application) {
    val contactsLive = ContactsLiveData(application.applicationContext)
    val mediaLive = MediaLiveData(application.applicationContext)

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

    fun getUser(user: User, activity: InitActivity) {
        RetroFitBuilder.apiService.getUser(user.number).enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.code() == 200) {
                    if (response.body()!!.isNotEmpty()) {

                        activity.initalizedUser()
                    } else {
                        RetroFitBuilder.apiService.createUser(user)
                            .enqueue(object : Callback<String> {
                                override fun onResponse(
                                    call: Call<String>,
                                    response: Response<String>
                                ) {
                                    if (response.code() == 200) {
                                        if (response.body()!!.isNotEmpty()) {
                                            activity.initalizedUser()
                                        }
                                    }
                                }

                                override fun onFailure(call: Call<String>, t: Throwable) {
                                    Log.d("TEST", "onResponse: ${t.message}")
                                }

                            })
                    }
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.d("TEST", "onResponse: ${t.message}")
            }

        })
    }
}