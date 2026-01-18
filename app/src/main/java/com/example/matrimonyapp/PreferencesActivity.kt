package com.example.matrimonyapp

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class PreferencesActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preferences)
        db = DatabaseHelper(this)
        userId = intent.getIntExtra("USER_ID", -1)
        if (userId <= 0) finish()

        val spPreferredGender = findViewById<Spinner>(R.id.spPreferredGender)
        val etMinAge = findViewById<EditText>(R.id.etMinAge)
        val etMaxAge = findViewById<EditText>(R.id.etMaxAge)
        val etMinHeight = findViewById<EditText>(R.id.etMinHeight)
        val etMaxHeight = findViewById<EditText>(R.id.etMaxHeight)
        val etReligion = findViewById<EditText>(R.id.etReligion)
        val etCaste = findViewById<EditText>(R.id.etCaste)
        val btnSave = findViewById<Button>(R.id.btnSavePreferences)

        // Setup spinner
        ArrayAdapter.createFromResource(
            this, R.array.gender_array, android.R.layout.simple_spinner_item
        ).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spPreferredGender.adapter = it
        }

        val genderSpinner: Spinner = findViewById(R.id.spPreferredGender)

        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.gender_array,
            R.layout.spinner_item // custom layout with romance_dark_red text
        )
        adapter.setDropDownViewResource(R.layout.spinner_item)
        genderSpinner.adapter = adapter

        // 🔹 Fetch logged-in user gender from Users table
        var loggedInGender = "Other"
        val userCursor = db.getUser(userId)   // <-- you need this in DatabaseHelper
        if (userCursor.moveToFirst()) {
            val genderIndex = userCursor.getColumnIndex("gender")
            if (genderIndex != -1) {
                loggedInGender = userCursor.getString(genderIndex) ?: "Other"
            }
        }
        userCursor.close()

        // 🔹 Set default preference: opposite gender
        val genderArray = resources.getStringArray(R.array.gender_array)
        val defaultGender = if (loggedInGender.equals("Male", true)) {
            "Female"
        } else {
            "Male"
        }

        // Find index and set selection
        val defaultIndex = genderArray.indexOfFirst { it.equals(defaultGender, true) }
        if (defaultIndex >= 0) {
            spPreferredGender.setSelection(defaultIndex)
        }

        btnSave.setOnClickListener {
            db.upsertPreferences(
                userId,
                spPreferredGender.selectedItem?.toString() ?: "Other",
                etMinAge.text.toString().toIntOrNull() ?: 18,
                etMaxAge.text.toString().toIntOrNull() ?: 99,
                etMinHeight.text.toString().toIntOrNull() ?: 0,
                etMaxHeight.text.toString().toIntOrNull() ?: 300,
                etReligion.text.toString(),
                etCaste.text.toString()
            )
            Toast.makeText(this, "Preferences saved!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}