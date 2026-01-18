package com.example.matrimonyapp

import android.content.Intent
import android.os.Bundle
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MatchListActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private lateinit var listView: ListView
    private var userId: Int = -1
    private lateinit var matchList: ArrayList<MatchRow>
    private lateinit var adapter: MatchAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_match_list)
try{
        db = DatabaseHelper(this)
        listView = findViewById(R.id.lvMatches)

        userId = intent.getIntExtra("USER_ID", -1)
        if (userId == -1) {
            Toast.makeText(this, "Invalid user session.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Check what type of request: search OR my matches
        val isMatches = intent.getBooleanExtra("IS_MATCHES", false)

        if (isMatches) {
            loadMyMatches()
        } else {
            val minAge = intent.getIntExtra("MIN_AGE", -1)
            val maxAge = intent.getIntExtra("MAX_AGE", -1)
            val minH = intent.getIntExtra("MIN_H", -1)
            val maxH = intent.getIntExtra("MAX_H", -1)
            val religion = intent.getStringExtra("RELIGION")
            val caste = intent.getStringExtra("CASTE")
            val gender = intent.getStringExtra("GENDER")

            loadSearchResults(
                minAge, maxAge,
                minH, maxH,
                religion, caste,
                gender
            )
        }

        // click listener → view profile
        listView.setOnItemClickListener { _, _, pos, _ ->
            val row = matchList[pos]
//            Toast.makeText(this, "UserId:" + row.userId, Toast.LENGTH_SHORT).show()
            val i = Intent(this, ProfileDetailsActivity::class.java)
            i.putExtra("LOGGED_IN_USER_ID", userId)
            i.putExtra("VIEWED_USER_ID", row.userId)
            startActivity(i)
        }
    }catch(e: Exception )
    {
        Toast.makeText(this,"hello"+(e.message.toString()),Toast.LENGTH_LONG).show()
    }

}

    private fun loadSearchResults(
        minAge: Int, maxAge: Int,
        minH: Int, maxH: Int,
        religion: String?, caste: String?,
        gender: String?
    )
    {
        try {
            matchList = ArrayList()
            val cursor = db.searchProfiles(
                currentUserId = userId,
                minAge = if (minAge > 0) minAge else null,
                maxAge = if (maxAge > 0) maxAge else null,
                minHeight = if (minH > 0) minH else null,
                maxHeight = if (maxH > 0) maxH else null,
                religion = religion?.takeIf { it.isNotBlank() },
                caste = caste?.takeIf { it.isNotBlank() },
                gender = gender?.takeIf { it.isNotBlank() }
            )
            while (cursor.moveToNext()) {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("user_id"))
                val name = cursor.getString(cursor.getColumnIndexOrThrow("username"))
                val pic = cursor.getString(cursor.getColumnIndexOrThrow("profile_picture"))
                val rel = cursor.getString(cursor.getColumnIndexOrThrow("religion")) ?: ""
                val cas = cursor.getString(cursor.getColumnIndexOrThrow("caste")) ?: ""
                val height = cursor.getInt(cursor.getColumnIndexOrThrow("height"))

                val subtitle = listOfNotNull(
                    if (height > 0) "${height}cm" else null,
                    if (rel.isNotBlank()) rel else null,
                    if (cas.isNotBlank()) cas else null
                ).joinToString(" • ")

                matchList.add(MatchRow(id, name, subtitle, pic))
            }
            cursor.close()

            adapter = MatchAdapter(this, matchList)
            listView.adapter = adapter
            if (matchList.isEmpty()) {
                Toast.makeText(this, "No results found.", Toast.LENGTH_SHORT).show()
            }
        }catch(e: Exception )
            {
                Toast.makeText(this,"load search result"+(e.message.toString()),Toast.LENGTH_LONG).show()
            }

        }

    private fun loadMyMatches() {
        try {
            matchList = ArrayList()
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

                matchList.add(
                    MatchRow(
                        userId = otherId,
                        title = otherName,
                        subtitle = subtitle,
                        imagePath = otherPic
                    )
                )
            }
            cursor.close()

            adapter = MatchAdapter(this, matchList)
            listView.adapter = adapter
            if (matchList.isEmpty()) {
                Toast.makeText(this, "No matches found.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "load matches " + (e.message.toString()), Toast.LENGTH_LONG).show()
        }
    }

}
