package com.danapps.letstalk.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.danapps.letstalk.models.Contact
import kotlinx.coroutines.flow.Flow

@Dao
interface Dao {

    @Query("SELECT * FROM contact")
    fun getSyncedContacts(): LiveData<List<Contact>>

    @Query("SELECT EXISTS(SELECT * FROM contact WHERE number = :number)")
    suspend fun checkSyncedContact(number: String): Boolean

    @Insert
    suspend fun insertSyncedContact(contact: Contact)
}