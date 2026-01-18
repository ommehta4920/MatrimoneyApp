package com.example.matrimonyapp

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class SearchFiltersActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_filters)
        try {
            db = DatabaseHelper(this)
            userId = intent.getIntExtra("USER_ID", -1)

            val etMinAge = findViewById<EditText>(R.id.etMinAge)
            val etMaxAge = findViewById<EditText>(R.id.etMaxAge)
            val etMinHeight = findViewById<EditText>(R.id.etMinHeight)
            val etMaxHeight = findViewById<EditText>(R.id.etMaxHeight)
            val etReligion = findViewById<EditText>(R.id.etReligion)
            val etCaste = findViewById<EditText>(R.id.etCaste)
            val spGender = findViewById<Spinner>(R.id.spGender)
            val btnSearch = findViewById<Button>(R.id.btnSearch)

            // Populate gender spinner
            val genderOptions = listOf("Any", "Male", "Female", "Other")
            val adapter = ArrayAdapter(
                this,
                R.layout.spinner_item, // custom TextView layout
                genderOptions
            )
            adapter.setDropDownViewResource(R.layout.spinner_item)
            spGender.adapter = adapter

            btnSearch.setOnClickListener {
                val minAge = etMinAge.text.toString().toIntOrNull()
                val maxAge = etMaxAge.text.toString().toIntOrNull()
                val minH = etMinHeight.text.toString().toIntOrNull()
                val maxH = etMaxHeight.text.toString().toIntOrNull()
                val rel = etReligion.text.toString()
                val cas = etCaste.text.toString()
                val gender = spGender.selectedItem.toString()

                // Navigate to MatchListActivity with all filters
                val i = Intent(this, MatchListActivity::class.java).apply {
                    putExtra("USER_ID", userId)
                    putExtra("MIN_AGE", minAge ?: -1)
                    putExtra("MAX_AGE", maxAge ?: -1)
                    putExtra("MIN_H", minH ?: -1)
                    putExtra("MAX_H", maxH ?: -1)
                    putExtra("RELIGION", rel)
                    putExtra("CASTE", cas)
                    putExtra("GENDER", if (gender == "Any") "" else gender)
                }
                startActivity(i)
            }

            if (userId <= 0) {
                Toast.makeText(
                    this,
                    "Tip: Login as user to get personalized flow.",
                     Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Search filter" + (e.message.toString()), Toast.LENGTH_LONG).show()
        }
    }
}
