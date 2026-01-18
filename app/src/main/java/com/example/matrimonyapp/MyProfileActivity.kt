package com.example.matrimonyapp

import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class MyProfileActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)

        db = DatabaseHelper(this)
        userId = intent.getIntExtra("USER_ID", -1)

        // UI references
        val mainPic = findViewById<ImageView>(R.id.ivProfilePic)
        val tvName = findViewById<TextView>(R.id.tvUsername)
        val tvAge = findViewById<TextView>(R.id.tvAge)
        val tvHeight = findViewById<TextView>(R.id.tvHeight)
        val tvWeight = findViewById<TextView>(R.id.tvWeight)
        val tvReligion = findViewById<TextView>(R.id.tvReligion)
        val tvCaste = findViewById<TextView>(R.id.tvCaste)
        val tvBio = findViewById<TextView>(R.id.tvBio)

        // Extended fields
        val tvDob = findViewById<TextView>(R.id.tvDob)
        val tvEmail = findViewById<TextView>(R.id.tvEmail)
        val tvPhone = findViewById<TextView>(R.id.tvPhone)
        val tvLocation = findViewById<TextView>(R.id.tvLocation)
        val tvEducation = findViewById<TextView>(R.id.tvEducation)
        val tvOccupation = findViewById<TextView>(R.id.tvOccupation)

        val profile = db.getProfile(userId)

        if (profile != null) {
            // Assign profile values
            tvName.text = profile.username?.uppercase() ?: "N/A"
            tvHeight.text = profile.height?.let { "$it cm" } ?: "N/A"
            tvWeight.text = profile.weight?.let { "$it kg" } ?: "N/A"
            tvReligion.text = profile.religion ?: "N/A"
            tvCaste.text = profile.caste ?: "N/A"
            tvBio.text = profile.bio ?: "N/A"

            // Age calculation
            val age = calculateAge(profile.dateOfBirth)
            tvAge.text = if (age > 0) "$age yrs" else "N/A"

            // Extra details
            tvDob.text = formatDob(profile.dateOfBirth)
            tvEmail.text = profile.email ?: "N/A"
            tvPhone.text = profile.phoneNumber ?: "N/A"
            tvLocation.text = profile.location ?: "N/A"
            tvEducation.text = profile.education ?: "N/A"
            tvOccupation.text = profile.occupation ?: "N/A"

            // Main photo
            // Main photo
            val mainPicPath = profile.profilePicture?.trim('"') ?: ""
            if (mainPicPath.isNotBlank()) {
                try {
                    mainPic.setImageURI(Uri.parse(mainPicPath))
                } catch (e: Exception) {
                    mainPic.setImageResource(R.drawable.img) // fallback
                }
            } else {
                mainPic.setImageResource(R.drawable.img) // fallback
            }

            // Load Photos
            val layoutPhotos = findViewById<LinearLayout>(R.id.layoutPhotos)
            val photosCursor: Cursor =
                db.getUserPhotos(userId) // ✅ use userId instead of viewedUserId
            layoutPhotos.removeAllViews()

            while (photosCursor.moveToNext()) {
                val uriStr =
                    photosCursor.getString(photosCursor.getColumnIndexOrThrow("photo_path"))
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
