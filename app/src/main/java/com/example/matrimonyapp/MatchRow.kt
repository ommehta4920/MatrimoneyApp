package com.example.matrimonyapp

data class MatchRow(
    val userId: Int,
    val title: String,       // Name or username
    val subtitle: String,    // Age, Religion, etc.
    val imagePath: String?   // Path/URI to profile image
)
