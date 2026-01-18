package com.example.matrimonyapp

data class MessageModel(
    val messageId: Int,
    val senderId: Int,
    val content: String,
    val sentAt: String,
    val isRead: Boolean
)
