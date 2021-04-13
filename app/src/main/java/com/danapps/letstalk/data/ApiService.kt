package com.danapps.letstalk.data

import com.danapps.letstalk.models.Contact
import com.danapps.letstalk.models.User
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {


    @POST("/api/syncContacts")
    fun syncContacts(@Body contactsArray: Array<Contact>): Call<Array<Contact>>

    @GET("/api/user/exists/{number}")
    fun userExists(@Path(value = "number") number: String): Call<Boolean>

    @POST("/api/create")
    fun createUser(@Body user: User): Call<String>

    @POST("/api/update")
    fun updateUser(@Body user: User): Call<String>
}