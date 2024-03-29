package com.danapps.letstalk.models

import androidx.room.Entity

@Entity
data class Chats(
    val msg: String,
    var name: String?,
    val profile_pic: String?,
    val who: String,
    val msgStats: Int?
)