package com.example.matrimonyapp

data class MatchRequest(
    val matchId: Int,
    val senderId: Int,
    val receiverId: Int,
    val username: String,
    val profilePic: String?,
    val status: String
)