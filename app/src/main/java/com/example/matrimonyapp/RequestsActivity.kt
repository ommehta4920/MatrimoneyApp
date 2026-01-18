package com.example.matrimonyapp

import android.database.Cursor
import android.os.Bundle
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

class RequestsActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private var userId: Int = -1
    private lateinit var listView: ListView
    private lateinit var adapter: MatchRequestAdapter
    private val requests = ArrayList<MatchRequest>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_requests)

        db = DatabaseHelper(this)
        userId = intent.getIntExtra("USER_ID", -1)
        if (userId <= 0) finish()

        listView = findViewById(R.id.lvUserRequests)
        adapter = MatchRequestAdapter(this, requests, db, userId, object : MatchRequestAdapter.OnRequestActionListener {
            override fun onActionDone() {
                loadRequests()  // refresh list immediately after Accept/Reject
            }
        })
        listView.adapter = adapter

        loadRequests()
    }

    override fun onResume() {
        super.onResume()
        loadRequests()
    }

    private fun loadRequests() {
        requests.clear()
        val c: Cursor = db.getUserMatches(userId)
        while (c.moveToNext()) {
            val matchId = c.getInt(c.getColumnIndexOrThrow("match_id"))
            val u1 = c.getInt(c.getColumnIndexOrThrow("user_id_1"))
            val u2 = c.getInt(c.getColumnIndexOrThrow("user_id_2"))
            val status = c.getString(c.getColumnIndexOrThrow("status"))

            val otherUserId = if (u1 == userId) u2 else u1
            val username = if (u1 == userId)
                c.getString(c.getColumnIndexOrThrow("u2name"))
            else
                c.getString(c.getColumnIndexOrThrow("u1name"))

            val profilePic = if (u1 == userId)
                c.getString(c.getColumnIndexOrThrow("u2pic"))
            else
                c.getString(c.getColumnIndexOrThrow("u1pic"))

            // only add statuses you want
            if (status == "Pending" || status == "Accepted" || status == "Rejected") {
                requests.add(
                    MatchRequest(
                        matchId = matchId,
                        senderId = u1,
                        receiverId = u2,
                        username = username,
                        profilePic = profilePic,
                        status = status
                    )
                )
            }
        }
        c.close()
        requests.reverse()
        adapter.notifyDataSetChanged()
    }
}

