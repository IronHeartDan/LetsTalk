package com.danapps.letstalk.data

import com.danapps.letstalk.models.User
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    @GET("/api/users")
    fun getUsers(): Call<String>

    @GET("/api/user/exists/{number}")
    fun userExists(@Path(value = "number") number: String): Call<Boolean>

    @POST("/api/create")
    fun createUser(@Body user: User): Call<String>
}