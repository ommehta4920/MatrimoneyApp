package com.example.matrimonyapp

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import android.widget.Button
import android.widget.EditText
import android.widget.ArrayAdapter
import android.widget.ListView

class MyInterestsActivity : AppCompatActivity() {
    private lateinit var db: DatabaseHelper
    private var userId: Int = -1
    private lateinit var adapter: ArrayAdapter<String>
    private val acceptedUsers = ArrayList<Pair<Int, String>>() // (id, name)
    private var selectedUserId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_interests)

        db = DatabaseHelper(this)
        userId = intent.getIntExtra("USER_ID", -1)

        val lv = findViewById<ListView>(R.id.lvAcceptedUsers)
        val etMessage = findViewById<EditText>(R.id.etInterestMessage)
        val btnSend = findViewById<Button>(R.id.btnSendInterest)

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, ArrayList())
        lv.adapter = adapter

        loadAcceptedUsers()

        lv.setOnItemClickListener { _, _, pos, _ ->
            selectedUserId = acceptedUsers[pos].first
            Toast.makeText(this, "Selected: ${acceptedUsers[pos].second}", Toast.LENGTH_SHORT).show()
        }

        btnSend.setOnClickListener {
            val msg = etMessage.text.toString()
            if (selectedUserId == null) {
                Toast.makeText(this, "Please select a user", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (msg.isBlank()) {
                Toast.makeText(this, "Enter a message", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val res = db.addInterest(userId, msg)
            if (res > 0) {
                Toast.makeText(this, "Interest sent!", Toast.LENGTH_SHORT).show()
                etMessage.text.clear()
            } else {
                Toast.makeText(this, "Failed to send", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadAcceptedUsers() {
        acceptedUsers.clear()
        val names = ArrayList<String>()

        val c = db.readableDatabase.rawQuery("""
            SELECT u.user_id, u.username
            FROM Matches m
            JOIN Users u 
              ON (CASE WHEN m.user_id_1=? THEN m.user_id_2 ELSE m.user_id_1 END) = u.user_id
            WHERE (m.user_id_1=? OR m.user_id_2=?)
              AND m.status='Accepted'
        """.trimIndent(), arrayOf(userId.toString(), userId.toString(), userId.toString()))

        while (c.moveToNext()) {
            val id = c.getInt(0)
            val name = c.getString(1)
            acceptedUsers.add(Pair(id, name))
            names.add(name)
        }
        c.close()

        adapter.clear()
        adapter.addAll(names)
        adapter.notifyDataSetChanged()
    }
}
