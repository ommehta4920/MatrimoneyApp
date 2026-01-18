package com.example.matrimonyapp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream

class UpdateProfileActivity : AppCompatActivity() {
    private var userId: Int = -1
    private lateinit var db: DatabaseHelper

    private lateinit var etUsername: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPhone: EditText
    private lateinit var imgProfilePic: ImageView
    private lateinit var btnChangePic: Button

    private lateinit var etHeight: EditText
    private lateinit var etWeight: EditText
    private lateinit var etReligion: EditText
    private lateinit var etCaste: EditText
    private lateinit var etLocation: EditText
    private lateinit var etEducation: EditText
    private lateinit var etOccupation: EditText
    private lateinit var etBio: EditText
    private lateinit var btnSave: Button

    private var savedProfilePicPath: String? = null
    private var currentUser: User? = null   // 🔹 store loaded user

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_profile)

        db = DatabaseHelper(this)
        userId = intent.getIntExtra("USER_ID", -1)

        initViews()
        loadUserAndProfile()

        btnChangePic.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                type = "image/*"
                addCategory(Intent.CATEGORY_OPENABLE)
            }
            startActivityForResult(intent, 100)
        }

        btnSave.setOnClickListener { saveProfile() }
    }

    private fun initViews() {
        etUsername = findViewById(R.id.etUsername)
        etEmail = findViewById(R.id.etEmail)
        etPhone = findViewById(R.id.etPhone)
        imgProfilePic = findViewById(R.id.imgProfilePic)
        btnChangePic = findViewById(R.id.btnChangePic)

        etHeight = findViewById(R.id.etHeight)
        etWeight = findViewById(R.id.etWeight)
        etReligion = findViewById(R.id.etReligion)
        etCaste = findViewById(R.id.etCaste)
        etLocation = findViewById(R.id.etLocation)
        etEducation = findViewById(R.id.etEducation)
        etOccupation = findViewById(R.id.etOccupation)
        etBio = findViewById(R.id.etBio)
        btnSave = findViewById(R.id.btnSave)
    }

    private fun loadUserAndProfile() {
        currentUser = db.getUserById(userId)
        val profile = db.getProfile(userId)

        currentUser?.let { user ->
            etUsername.setText(user.username)
            etEmail.setText(user.email)
            etPhone.setText(user.phoneNumber)

            if (!user.profilePicture.isNullOrBlank()) {
                val file = File(user.profilePicture)
                if (file.exists()) {
                    imgProfilePic.setImageURI(Uri.fromFile(file))
                    savedProfilePicPath = user.profilePicture
                }
            }
        }

        profile?.let {
            etHeight.setText(it.height?.toString() ?: "")
            etWeight.setText(it.weight?.toString() ?: "")
            etReligion.setText(it.religion ?: "")
            etCaste.setText(it.caste ?: "")
            etLocation.setText(it.location ?: "")
            etEducation.setText(it.education ?: "")
            etOccupation.setText(it.occupation ?: "")
            etBio.setText(it.bio ?: "")
        }
    }

    private fun saveProfile() {
        val username = etUsername.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val phone = etPhone.text.toString().trim()

        val height = etHeight.text.toString().toIntOrNull() ?: 0
        val weight = etWeight.text.toString().toIntOrNull() ?: 0
        val religion = etReligion.text.toString().trim()
        val caste = etCaste.text.toString().trim()
        val location = etLocation.text.toString().trim()
        val education = etEducation.text.toString().trim()
        val occupation = etOccupation.text.toString().trim()
        val bio = etBio.text.toString().trim()


        val profilePic = if (!savedProfilePicPath.isNullOrBlank()) {
            savedProfilePicPath!!
        } else {
            currentUser?.profilePicture.orEmpty()
        }
        val userUpdated = db.updateUser(userId, username, email, phone, profilePic)

        val profileUpdated = db.upsertProfile(
            userId, height, weight, religion, caste, location, education, occupation, bio
        )

// Save profile picture also in UserPhotos if a new one was selected
        var photoInserted = true
        if (!savedProfilePicPath.isNullOrBlank()) {
            val photoResult = db.insertUserPhoto(userId, savedProfilePicPath!!)
            photoInserted = photoResult != -1L
        }

        if (userUpdated && profileUpdated != -1L && photoInserted) {
            Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
            setResult(Activity.RESULT_OK, Intent().apply { putExtra("UPDATED", true) })
            finish()
        } else {
            Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            val uri = data?.data
            if (uri != null) {
                savedProfilePicPath = copyUriToInternalStorage(uri)
                val file = File(savedProfilePicPath!!)
                imgProfilePic.setImageURI(Uri.fromFile(file))
            }
        }
    }

    // 🔹 Copies selected image into internal storage, returns file path
    private fun copyUriToInternalStorage(uri: Uri): String {
        val inputStream = contentResolver.openInputStream(uri) ?: return ""
        val file = File(filesDir, "profile_${System.currentTimeMillis()}.jpg")
        inputStream.use { input ->
            FileOutputStream(file).use { output -> input.copyTo(output) }
        }
        return file.absolutePath
    }
}
