package com.danapps.letstalk.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class User(
    val name: String,
    @PrimaryKey
    val number: String,
    val profile_pic: String?
)