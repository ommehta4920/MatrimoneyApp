package com.example.matrimonyapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        db = DatabaseHelper(this)

        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnGoToRegister = findViewById<TextView>(R.id.tvRegister)

        btnLogin.setOnClickListener {
            val loginInput = etUsername.text.toString().trim()   // actually email/phone
            val password = etPassword.text.toString().trim()

            if (loginInput.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter Email/Phone and Password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Admin hardcoded login
            if (loginInput == "admin" && password == "12345") {
                startActivity(Intent(this, AdminDashboardActivity::class.java))
                finish()
                return@setOnClickListener
            }

            val userId = db.checkUserLogin(loginInput, password)
            if (userId != null) {
                Toast.makeText(this, "Welcome!", Toast.LENGTH_SHORT).show()
                val i = Intent(this, UserDashboardActivity::class.java)
                i.putExtra("USER_ID", userId)
                startActivity(i)
                finish()
            } else {
                Toast.makeText(this, "Invalid credentials or account not verified/blocked.", Toast.LENGTH_LONG).show()
            }
        }

        btnGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}
