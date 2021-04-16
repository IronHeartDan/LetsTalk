package com.danapps.letstalk.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ChatMessage(
    var conId: String?,
    val from: String,
    val to: String,
    val msg: String,
    var msgStats: Int?
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}