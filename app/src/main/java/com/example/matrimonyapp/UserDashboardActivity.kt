    package com.example.matrimonyapp

    import android.content.Intent
    import android.os.Bundle
    import android.widget.TextView
    import android.widget.Toast
    import androidx.appcompat.app.AppCompatActivity
    import androidx.cardview.widget.CardView

    class UserDashboardActivity : AppCompatActivity() {

        private var userId: Int = -1
        private lateinit var tvWelcome: TextView
        private lateinit var tvProfileDetails: TextView
        private lateinit var cardSetProfile: CardView
        private lateinit var db: DatabaseHelper

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_user_dashboard)

            userId = intent.getIntExtra("USER_ID", -1)

            db = DatabaseHelper(this)
            val username = db.getUsername(userId) ?: "User"

            tvWelcome = findViewById(R.id.tvWelcome)
            tvProfileDetails = findViewById(R.id.tvProfileDetails)
            cardSetProfile = findViewById(R.id.cardSetProfile)

            tvWelcome.text = "Welcome, ${username.toWordsTitleCase()}!"

            val cardProfile = findViewById<CardView>(R.id.cardProfile)
            val cardPreferences = findViewById<CardView>(R.id.cardPreferences)
            val cardSearch = findViewById<CardView>(R.id.cardSearch)
            val cardMatches = findViewById<CardView>(R.id.cardMatches)
            val cardRequests = findViewById<CardView>(R.id.cardRequests)
            val cardMessages = findViewById<CardView>(R.id.cardMessages)
            val cardLogout = findViewById<CardView>(R.id.cardLogout)

            // ---- My Profile ----
            cardProfile.setOnClickListener {
                val i = Intent(this, MyProfileActivity::class.java)
                i.putExtra("USER_ID", userId)
                startActivity(i)
            }

            // ---- Preferences ----
            cardPreferences.setOnClickListener {
                val i = Intent(this, PreferencesActivity::class.java)
                i.putExtra("USER_ID", userId)
                startActivity(i)
            }

            // ---- Search Matches ----
            cardSearch.setOnClickListener {
                val i = Intent(this, SearchFiltersActivity::class.java)
                i.putExtra("USER_ID", userId)
                startActivity(i)
            }

            // ---- My Matches ----
            cardMatches.setOnClickListener {
                val i = Intent(this, MyMatchesActivity::class.java)
                i.putExtra("USER_ID", userId)
                startActivity(i)
            }

            // ---- Requests ----
            cardRequests.setOnClickListener{
                val i = Intent(this, RequestsActivity::class.java)
                i.putExtra("USER_ID", userId)
                startActivity(i)
            }

            // ---- Messages ----
            cardMessages.setOnClickListener {
                val i = Intent(this, MessagingListActivity::class.java)
                i.putExtra("USER_ID", userId)
                startActivity(i)
            }

            // ---- Logout ----
            cardLogout.setOnClickListener {
                val i = Intent(this, LoginActivity::class.java)
                startActivity(i)
                finish()
            }
        }

        override fun onResume() {
            super.onResume()
            updateProfileCard()
        }

        private fun updateProfileCard() {
            if (db.hasUserProfile(userId)) {
                tvProfileDetails.text = "Update Profile"
                cardSetProfile.setOnClickListener {
                    val i = Intent(this, UpdateProfileActivity::class.java)
                    i.putExtra("USER_ID", userId)
                    startActivity(i)
                }
            } else {
                tvProfileDetails.text = "Set Profile"
                cardSetProfile.setOnClickListener {
                    val i = Intent(this, ProfileSetupActivity::class.java)
                    i.putExtra("USER_ID", userId)
                    startActivity(i)
                }
            }
        }

        fun String.toWordsTitleCase(): String =
            this.lowercase().split(" ").joinToString(" ") { word ->
                word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            }
    }

