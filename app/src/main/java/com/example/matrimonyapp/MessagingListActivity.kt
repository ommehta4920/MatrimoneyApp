package com.example.matrimonyapp

import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

class MessagingListActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private var userId: Int = -1
    private lateinit var lvChats: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_messaging_list)

        db = DatabaseHelper(this)
        userId = intent.getIntExtra("USER_ID", -1)
        lvChats = findViewById(R.id.lvChats)
    }

    override fun onResume() {
        super.onResume()
        loadChats()
    }

    private fun loadChats() {
        val chatList = ArrayList<HashMap<String, String>>()

        // Fetch distinct conversations sorted by latest message
        val cursor: Cursor = db.readableDatabase.rawQuery(
            """
            SELECT m.match_id,
                   CASE 
                       WHEN m.user_id_1 = ? THEN u2.username 
                       ELSE u1.username 
                   END AS otherUser,
                   msg.message_content AS lastMessage,
                   CASE 
                       WHEN m.user_id_1 = ? THEN u2.profile_picture 
                       ELSE u1.profile_picture 
                   END AS profilePic,
                   msg.latest_time
            FROM Matches m
            JOIN Users u1 ON m.user_id_1 = u1.user_id
            JOIN Users u2 ON m.user_id_2 = u2.user_id
            LEFT JOIN (
                SELECT match_id, message_content, MAX(sent_at) as latest_time
                FROM Messages
                GROUP BY match_id
            ) msg ON m.match_id = msg.match_id
            WHERE (m.user_id_1 = ? OR m.user_id_2 = ?) 
              AND m.status = 'Accepted'
            ORDER BY msg.latest_time DESC
            """.trimIndent(),
            arrayOf(userId.toString(), userId.toString(), userId.toString(), userId.toString())
        )

        while (cursor.moveToNext()) {
            val map = HashMap<String, String>()
            map["match_id"] = cursor.getInt(0).toString()
            val otherUser = cursor.getString(1)?.toTitleCase() ?: "Unknown"
            map["otherUser"] = otherUser
            map["lastMessage"] = cursor.getString(2) ?: "Tap to chat"
            map["profilePic"] = cursor.getString(3) ?: ""
            chatList.add(map)
        }
        cursor.close()

        val adapter = ChatListAdapter(this, chatList)
        lvChats.adapter = adapter

        lvChats.setOnItemClickListener { _, _, position, _ ->
            val matchId = chatList[position]["match_id"]!!.toInt()
            val otherUserName = chatList[position]["otherUser"]

            // Find the other user's ID
            val c = db.readableDatabase.rawQuery(
                "SELECT user_id_1, user_id_2 FROM Matches WHERE match_id=?",
                arrayOf(matchId.toString())
            )
            var otherId = -1
            if (c.moveToFirst()) {
                val u1 = c.getInt(0)
                val u2 = c.getInt(1)
                otherId = if (u1 == userId) u2 else u1
            }
            c.close()

            val i = Intent(this, MessagingActivity::class.java).apply {
                putExtra("USER_ID", userId)
                putExtra("MATCH_ID", matchId)
                putExtra("OTHER_ID", otherId)
                putExtra("OTHER_NAME", otherUserName)
            }
            startActivity(i)
        }
    }

    private fun String.toTitleCase(): String =
        this.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}
