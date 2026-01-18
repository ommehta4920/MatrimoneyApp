package com.example.matrimonyapp

data class ProfileDetails(
    val userId: Int,
    val username: String?,
    val profilePicture: String?,
    val dateOfBirth: String?,
    val email: String?,
    val phoneNumber: String?,
    val height: Int?,
    val weight: Int?,
    val religion: String?,
    val caste: String?,
    val location: String?,
    val education: String?,
    val occupation: String?,
    val bio: String?
)
