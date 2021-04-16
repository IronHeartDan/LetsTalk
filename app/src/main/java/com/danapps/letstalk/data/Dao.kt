package com.danapps.letstalk.data

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.Dao
import com.danapps.letstalk.models.ChatMessage
import com.danapps.letstalk.models.Chats
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

    @Query("SELECT EXISTS(SELECT * FROM contact WHERE number=:number)")
    suspend fun contactExists(number: String): Boolean

    @Query("SELECT * FROM contact WHERE number=:number")
    suspend fun getContact(number: String): List<Contact>

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
    fun getConversation(from: String, to: String): LiveData<List<ChatMessage>>

//    @Query("SELECT  u1.name, u1.profile_pic, u2.name , u2.profile_pic , c.msgStats, c.msg ,c.conId    FROM chatmessage c JOIN contact u1 ON c.`from` = u1.number JOIN contact u2 ON c.`to` = u2.number WHERE c.id IN (SELECT MAX(Id) FROM chatmessage GROUP BY conId) ORDER BY c.id DESC ")
//    fun getChats(): LiveData<List<Chats>>


    @Query("SELECT  name,profile_pic,msg, who  from contact  c join (SELECT  msg ,  CASE WHEN  `from` = :number THEN `to`  ELSE  `from`  END as who  FROM chatmessage  c  WHERE id IN (SELECT MAX(id)  FROM chatmessage GROUP BY conId) ORDER BY id DESC) as vt on c.number=vt.who;")
    fun getChats(number: String): LiveData<List<Chats>>

}