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

    @Update
    suspend fun updateUser(user: User)

    @Query("SELECT EXISTS(SELECT * FROM user WHERE number = :number)")
    suspend fun userExists(number: String): Boolean

    @Query("SELECT * FROM user")
    suspend fun getUser(): List<User>

    @Query("SELECT * FROM user WHERE number = :number")
    fun liveUser(number: String): LiveData<User>

    @Query("DELETE FROM user")
    suspend fun logOut()

    @Query("SELECT * FROM contact ORDER BY name ASC")
    fun getSyncedContacts(): LiveData<List<Contact>>

    @Insert
    suspend fun insertSyncedContact(contact: Contact)

    @Delete
    suspend fun deleteSyncedContact(contact: Contact)

    @Query("DELETE FROM contact")
    suspend fun deleteContacts()


    //Chat System


    @Insert
    suspend fun insertChat(chatMessage: ChatMessage): Long

    @Update
    suspend fun updateChat(chatMessage: ChatMessage)

    @Query("UPDATE chatmessage set msgStats=3 WHERE `from` = :from AND `to` = :to  AND msgStats != 3")
    suspend fun markSeen(from: String, to: String)

    @Query("SELECT * FROM chatmessage WHERE `from` = :from AND `to` = :to OR `from` = :to AND `to` = :from")
    fun getChats(from: String, to: String): LiveData<List<ChatMessage>>
}