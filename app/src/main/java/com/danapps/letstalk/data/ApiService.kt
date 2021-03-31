package com.danapps.letstalk.data

import retrofit2.Call
import retrofit2.http.GET

interface ApiService {

    @GET("/api/users")
    fun getUsers(): Call<String>
}