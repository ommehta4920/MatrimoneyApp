package com.example.matrimonyapp

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class ProfileSetupActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private var userId: Int = -1

    // Use the new Activity Result API for multiple photos
//    private val pickMultiple =
//        registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
//            if (uris != null && uris.isNotEmpty()) {
//                var firstPhotoSaved = false
//                for (uri in uris) {
//                    val path = persistAndGetPath(uri)
//                    db.addPhoto(userId, path)
//
//                    // Save first photo also as main profile picture
//                    if (!firstPhotoSaved) {
//                        db.updateUserProfilePicture(userId, path)
//                        firstPhotoSaved = true
//                    }
//                }
//                Toast.makeText(this, "Photos added!", Toast.LENGTH_SHORT).show()
//            } else {
//                Toast.makeText(this, "No photo selected", Toast.LENGTH_SHORT).show()
//            }
//        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_setup)

        db = DatabaseHelper(this)
        userId = intent.getIntExtra("USER_ID", -1)
        if (userId <= 0) finish()

        val etHeight = findViewById<EditText>(R.id.etHeight)
        val etWeight = findViewById<EditText>(R.id.etWeight)
        val etReligion = findViewById<EditText>(R.id.etReligion)
        val etCaste = findViewById<EditText>(R.id.etCaste)
        val etLocation = findViewById<EditText>(R.id.etLocation)
        val etEducation = findViewById<EditText>(R.id.etEducation)
        val etOccupation = findViewById<EditText>(R.id.etOccupation)
        val etBio = findViewById<EditText>(R.id.etBio)
//        val btnUploadPhotos = findViewById<Button>(R.id.btnUploadPhotos)
        val btnSaveProfile = findViewById<Button>(R.id.btnSaveProfile)

//        btnUploadPhotos.setOnClickListener {
//            // allow multiple image selection
//            pickMultiple.launch(arrayOf("image/*"))
//        }

        btnSaveProfile.setOnClickListener {
            val res = db.upsertProfile(
                userId,
                etHeight.text.toString().toIntOrNull() ?: 0,
                etWeight.text.toString().toIntOrNull() ?: 0,
                etReligion.text.toString(),
                etCaste.text.toString(),
                etLocation.text.toString(),
                etEducation.text.toString(),
                etOccupation.text.toString(),
                etBio.text.toString()
            )
            if (res >= 0) {
                Toast.makeText(this, "Profile saved!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Failed to save profile", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun persistAndGetPath(uri: Uri): String {
        val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        try {
            // Persist permission so URI works after app restart
            contentResolver.takePersistableUriPermission(uri, takeFlags)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
        return uri.toString()
    }
}
