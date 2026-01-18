package com.example.matrimonyapp

import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class ProfileDetailsActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private var currentUserId: Int = -1
    private var viewedUserId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_details)

        db = DatabaseHelper(this)
        currentUserId = intent.getIntExtra("LOGGED_IN_USER_ID", -1)
        viewedUserId = intent.getIntExtra("VIEWED_USER_ID", -1)

        // UI elements
        val imgMainPhoto = findViewById<ImageView>(R.id.imgMainPhoto)
        val tvUsername = findViewById<TextView>(R.id.tvUsername)
        val tvAge = findViewById<TextView>(R.id.tvAge)
        val tvHeight = findViewById<TextView>(R.id.tvHeight)
        val tvReligion = findViewById<TextView>(R.id.tvReligion)
        val tvCaste = findViewById<TextView>(R.id.tvCaste)
        val tvLocation = findViewById<TextView>(R.id.tvLocation)
        val tvEducation = findViewById<TextView>(R.id.tvEducation)
        val tvOccupation = findViewById<TextView>(R.id.tvOccupation)
        val tvBio = findViewById<TextView>(R.id.tvBio)
        val layoutPhotos = findViewById<LinearLayout>(R.id.layoutPhotos)
        val btnSendRequest = findViewById<Button>(R.id.btnSendRequest)
        val tvConnectionStatus = findViewById<TextView>(R.id.tvConnectionStatus)
        val llStatus = findViewById<LinearLayout>(R.id.llStatus)

        // Newly added Personal Info fields
        val tvDob = findViewById<TextView>(R.id.tvDob)
        val tvEmail = findViewById<TextView>(R.id.tvEmail)
        val tvPhone = findViewById<TextView>(R.id.tvPhone)

        // Load Profile Info
        val profile = db.getProfile(viewedUserId)

        if (profile != null) {
            val username = profile.username ?: ""
            val mainPic = profile.profilePicture ?: ""
            val dobString = profile.dateOfBirth
            val height = profile.height ?: 0
            val religion = profile.religion ?: ""
            val caste = profile.caste ?: ""
            val location = profile.location ?: ""
            val education = profile.education ?: ""
            val occupation = profile.occupation ?: ""
            val bio = profile.bio ?: ""
            val email = profile.email ?: ""
            val phone = profile.phoneNumber ?: ""

            // Calculate age from Date of Birth
            val age = calculateAge(dobString)

            // Set values in UI
            tvUsername.text = username.uppercase()
            tvAge.text = "$age yrs"
            tvHeight.text = if (height > 0) "${height} cm" else "N/A"
            tvReligion.text = religion
            tvCaste.text = caste
            tvLocation.text = location
            tvEducation.text = education
            tvOccupation.text = occupation
            tvBio.text = bio

            // Personal Info values
            tvDob.text = formatDob(dobString)
            tvEmail.text = email
            tvPhone.text = phone

            // Main photo
            if (mainPic.isNotBlank()) {
                try {
                    imgMainPhoto.setImageURI(Uri.parse(mainPic))
                } catch (e: Exception) {
                    imgMainPhoto.setImageResource(R.drawable.img) // fallback
                }
            } else {
                imgMainPhoto.setImageResource(R.drawable.img) // fallback
            }
        }

        // Load Photos
        val photosCursor: Cursor = db.getUserPhotos(viewedUserId)
        layoutPhotos.removeAllViews()
        while (photosCursor.moveToNext()) {
            val uriStr = photosCursor.getString(photosCursor.getColumnIndexOrThrow("photo_path"))
            val img = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(250, 250).apply {
                    marginEnd = 16
                }
                scaleType = ImageView.ScaleType.CENTER_CROP
            }

            try {
                img.setImageURI(Uri.parse(uriStr))
            } catch (_: Exception) {
                img.setImageResource(R.drawable.img)
            }
            layoutPhotos.addView(img)
        }
        photosCursor.close()

        // Check Match Status before showing button
        var matchStatus: String? = null
        val matchCursor: Cursor = db.getMatchBetweenUsers(currentUserId, viewedUserId)
        if (matchCursor.moveToFirst()) {
            matchStatus = matchCursor.getString(matchCursor.getColumnIndexOrThrow("status"))
        }
        matchCursor.close()

        when (matchStatus) {
            "Accepted" -> {
                btnSendRequest.visibility = View.GONE
                llStatus.visibility = View.VISIBLE
                tvConnectionStatus.text = "You are already connected!"
            }
            "Pending" -> {
                btnSendRequest.visibility = View.VISIBLE
                btnSendRequest.isEnabled = false
                btnSendRequest.text = "Request Sent"
                llStatus.visibility = View.GONE
            }
            else -> {
                btnSendRequest.visibility = View.VISIBLE
                btnSendRequest.isEnabled = true
                btnSendRequest.text = "Send Request"
                llStatus.visibility = View.GONE

                btnSendRequest.setOnClickListener {
                    if (currentUserId > 0 && viewedUserId > 0) {
                        val success = db.sendMatchRequest(currentUserId, viewedUserId)
                        if (success) {
                            Toast.makeText(this, "Request sent!", Toast.LENGTH_SHORT).show()
                            btnSendRequest.isEnabled = false
                            btnSendRequest.text = "Request Sent"
                        } else {
                            Toast.makeText(this, "Something went wrong!", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "Error: Invalid user.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun calculateAge(dateOfBirth: String?): Int {
        if (dateOfBirth.isNullOrBlank()) return 0
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val dob = Calendar.getInstance()
            dob.time = sdf.parse(dateOfBirth)!!

            val today = Calendar.getInstance()
            var age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR)
            if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
                age--
            }
            age
        } catch (e: Exception) {
            0
        }
    }

    private fun formatDob(dobString: String?): String {
        if (dobString.isNullOrBlank()) return "N/A"
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.US)
            outputFormat.format(inputFormat.parse(dobString)!!)
        } catch (e: Exception) {
            dobString
        }
    }
}
