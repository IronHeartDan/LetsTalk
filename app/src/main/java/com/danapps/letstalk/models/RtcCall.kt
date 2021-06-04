package com.danapps.letstalk.models

data class RtcCall(
    var offer: String?,
    val from: String,
    val to: String,
    val callType: Int = 0,
    val offerType: Int = 0
)