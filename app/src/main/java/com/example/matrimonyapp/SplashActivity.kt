package com.example.matrimonyapp

import androidx.appcompat.app.AppCompatActivity

// For using Handler (deprecated in API 30+)
import android.os.Handler
import android.os.Looper
import android.os.Bundle
import android.content.Intent
// For using Coroutines (recommended alternative)
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.security.auth.login.LoginException

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Using coroutines for delay
        lifecycleScope.launch {
            delay(2000) // 2 seconds delay
            startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
            finish()
        }

        // Alternative using Handler (deprecated in API 30)
        // Handler(Looper.getMainLooper()).postDelayed({
        //     startActivity(Intent(this, MainActivity::class.java))
        //     finish()
        // }, 2000)
    }
}