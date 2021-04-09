package com.danapps.letstalk.data

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.Dao
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
    suspend fun logOut()

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
    suspend fun insertChat(chatMessage: ChatMessage): Long

    @Update
    suspend fun updateChat(chatMessage: ChatMessage)

    @Query("UPDATE chatmessage set msgStats=3 WHERE `from` = :from AND `to` = :to ")
    suspend fun markSeen(from: String, to: String)

    @Query("SELECT * FROM chatmessage WHERE `from` = :from AND `to` = :to OR `from` = :to AND `to` = :from")
    fun getChats(from: String, to: String): LiveData<List<ChatMessage>>
}