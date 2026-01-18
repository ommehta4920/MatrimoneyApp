package com.example.matrimonyapp

import android.content.Intent
import android.os.Bundle
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MyMatchesActivity : AppCompatActivity() {
    private lateinit var db: DatabaseHelper
    private lateinit var listView: ListView
    private lateinit var adapter: MatchAdapter
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_matches)

        db = DatabaseHelper(this)
        userId = intent.getIntExtra("USER_ID", -1)

        listView = findViewById(R.id.lvMyMatches)
        adapter = MatchAdapter(this, ArrayList()) // start with empty list
        listView.adapter = adapter

        loadMyMatches()

        listView.setOnItemClickListener { _, _, pos, _ ->
            val row = adapter.getItem(pos) as MatchRow
            val i = Intent(this, ProfileDetailsActivity::class.java)
            i.putExtra("LOGGED_IN_USER_ID", userId)
            i.putExtra("VIEWED_USER_ID", row.userId)
            startActivity(i)
        }
    }

    private fun loadMyMatches() {
        try {
            val newList = ArrayList<MatchRow>()
            val cursor = db.getMatchesByPreferences(userId)

            while (cursor.moveToNext()) {
                val otherId = cursor.getInt(cursor.getColumnIndexOrThrow("user_id"))
                val otherName = cursor.getString(cursor.getColumnIndexOrThrow("username"))
                val otherPic = cursor.getString(cursor.getColumnIndexOrThrow("profile_picture"))
                val rel = cursor.getString(cursor.getColumnIndexOrThrow("religion")) ?: ""
                val cas = cursor.getString(cursor.getColumnIndexOrThrow("caste")) ?: ""
                val height = cursor.getInt(cursor.getColumnIndexOrThrow("height"))

                val subtitle = listOfNotNull(
                    if (height > 0) "${height}cm" else null,
                    if (rel.isNotBlank()) rel else null,
                    if (cas.isNotBlank()) cas else null
                ).joinToString(" • ")

                newList.add(MatchRow(otherId, otherName, subtitle, otherPic))
            }
            cursor.close()

            adapter.updateData(newList)

            if (newList.isEmpty()) {
                Toast.makeText(this, "No matches found.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error loading matches: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
