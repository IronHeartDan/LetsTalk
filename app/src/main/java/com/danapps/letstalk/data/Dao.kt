package com.danapps.letstalk.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.danapps.letstalk.models.ChatMessage
import com.danapps.letstalk.models.Contact
import com.danapps.letstalk.models.User

@Dao
interface Dao {

    @Insert
    suspend fun createUser(user: User)

    @Query("SELECT EXISTS(SELECT * FROM user WHERE number = :number)")
    suspend fun getUser(number: String): Boolean

    @Query("DELETE FROM user")
    suspend fun deleteUser()

    @Query("SELECT * FROM contact ORDER BY name ASC")
    fun getSyncedContacts(): LiveData<List<Contact>>

    @Query("SELECT EXISTS(SELECT * FROM contact WHERE number = :number)")
    suspend fun checkSyncedContact(number: String): Boolean

    @Insert
    suspend fun insertSyncedContact(contact: Contact)

    @Delete
    suspend fun deleteSyncedContact(contact: Contact)


    //Chat System


    @Insert
    suspend fun insertChat(chatMessage: ChatMessage)
}