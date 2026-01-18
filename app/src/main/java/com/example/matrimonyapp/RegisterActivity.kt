package com.example.matrimonyapp

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Patterns
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import android.content.Intent
import java.util.*

class RegisterActivity : AppCompatActivity() {
    private lateinit var db: DatabaseHelper
    private var selectedImagePath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        db = DatabaseHelper(this)

        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPhone = findViewById<EditText>(R.id.etPhone)
        val spGender = findViewById<Spinner>(R.id.spGender)
        val etDob = findViewById<EditText>(R.id.etDob)
        val ivProfilePic = findViewById<ImageView>(R.id.ivProfilePic)
        val btnChoosePic = findViewById<Button>(R.id.btnChoosePic)
        val btnRegister = findViewById<Button>(R.id.btnRegister)

        val genderSpinner: Spinner = findViewById(R.id.spGender)

        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.gender_array,
            R.layout.spinner_item // custom layout with romance_dark_red text
        )
        adapter.setDropDownViewResource(R.layout.spinner_item)
        genderSpinner.adapter = adapter

        // Calendar instance for current date
        val calendar = Calendar.getInstance()

        // Show DatePicker when clicking DOB field
        etDob.setOnClickListener {
            val calendar = Calendar.getInstance()

            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(
                this,
                R.style.MyDatePickerTheme,
                { _, selectedYear, selectedMonth, selectedDay ->
                    val pickedDate = Calendar.getInstance()
                    pickedDate.set(selectedYear, selectedMonth, selectedDay)

                    val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                    etDob.setText(sdf.format(pickedDate.time))
                },
                year, month, day
            )

            // Set maximum date to 18 years ago from current year, on Dec 31
            val maxDate = Calendar.getInstance()
            maxDate.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR) - 18)
            maxDate.set(Calendar.MONTH, 11) // December
            maxDate.set(Calendar.DAY_OF_MONTH, 31)
            datePicker.datePicker.maxDate = maxDate.timeInMillis

            // Set minimum date to 100 years ago from current year, on Jan 1
            val minDate = Calendar.getInstance()
            minDate.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR) - 100)
            minDate.set(Calendar.MONTH, 0) // January
            minDate.set(Calendar.DAY_OF_MONTH, 1)
            datePicker.datePicker.minDate = minDate.timeInMillis

            datePicker.show()

        }

        btnChoosePic.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply { type = "image/*" }
            startActivityForResult(intent, 101)
        }

        btnRegister.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val gender = spGender.selectedItem.toString()
            val dob = etDob.text.toString().trim()

            // Validation checks
            when {
                username.isEmpty() -> {
                    etUsername.error = "Username is required"
                    etUsername.requestFocus()
                    return@setOnClickListener
                }
                password.length < 6 -> {
                    etPassword.error = "Password must be at least 6 characters"
                    etPassword.requestFocus()
                    return@setOnClickListener
                }
                email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    etEmail.error = "Enter a valid email"
                    etEmail.requestFocus()
                    return@setOnClickListener
                }
                phone.isEmpty() || phone.length != 10 || !phone.all { it.isDigit() } -> {
                    etPhone.error = "Enter a valid 10-digit phone number"
                    etPhone.requestFocus()
                    return@setOnClickListener
                }
                gender.equals("Select Gender", ignoreCase = true) -> {
                    Toast.makeText(this, "Please select a gender", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                dob.isEmpty() -> {
                    etDob.error = "Please select your Date of Birth"
                    etDob.requestFocus()
                    return@setOnClickListener
                }
//                selectedImagePath.isNullOrBlank() -> {
//                    Toast.makeText(this, "Please select a profile picture", Toast.LENGTH_SHORT).show()
//                    return@setOnClickListener
//                }
            }

            // Check if email or phone already exists
            if (db.isEmailOrPhoneExists(email, phone)) {
                Toast.makeText(this, "Email or phone number already exists", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // If validation passes, insert into DB
            val id = db.insertUser(username, password, email, phone, gender, dob)
            if (id > 0) {
                if (!selectedImagePath.isNullOrBlank()) {
                    // Save in Users Table
                    db.updateUserProfilePic(id.toInt(), selectedImagePath!!)

                    // Save in UserPhotos Table
                    db.addUserPhoto(id.toInt(), selectedImagePath!!)
                }
                Toast.makeText(this, "Registered successfully", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Registration failed, please try again", Toast.LENGTH_SHORT).show()
            }
        }

    }

    override fun onActivityResult(req: Int, res: Int, data: Intent?) {
        super.onActivityResult(req, res, data)
        if (req == 101 && res == RESULT_OK) {
            val uri = data?.data ?: return
            // Persist permission so the app can access it later
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )

            selectedImagePath = uri.toString()
            findViewById<ImageView>(R.id.ivProfilePic).setImageURI(uri)
        }
    }
}