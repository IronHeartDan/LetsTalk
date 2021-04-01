package com.danapps.letstalk.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.danapps.letstalk.models.Contact

@Database(entities = [Contact::class], version = 1)
abstract class LetsTalkDatabase : RoomDatabase() {

    abstract fun dao(): Dao


    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: LetsTalkDatabase? = null

        fun getDatabase(context: Context): LetsTalkDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LetsTalkDatabase::class.java,
                    "letstalk_database"
                ).build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}