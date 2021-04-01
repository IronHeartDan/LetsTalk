package com.danapps.letstalk.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Contact(
    val name: String,
    val profile_pic: String?,
    @PrimaryKey
    val number: String
)