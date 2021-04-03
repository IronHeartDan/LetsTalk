package com.danapps.letstalk.data

import android.content.Context
import androidx.room.*
import com.danapps.letstalk.models.ChatMessage
import com.danapps.letstalk.models.Contact
import com.danapps.letstalk.models.User
import java.util.*

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time?.toLong()
    }
}

@Database(entities = [User::class, Contact::class, ChatMessage::class], version = 3)
@TypeConverters(Converters::class)
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
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}