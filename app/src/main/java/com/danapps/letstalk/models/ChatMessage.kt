package com.danapps.letstalk.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class ChatMessage(
    val from: String,
    val to: String,
    val msg: String,
    val timeStamp: Date
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}