package com.example.matrimonyapp

import MessageChatAdapter
import android.database.Cursor
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MessagingActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private var userId: Int = -1
    private var matchId: Int = -1
    private var otherId: Int = -1
    private var otherUsername: String? = null

    private lateinit var lv: ListView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: Button
    private lateinit var tvChatHeader: TextView
    private val msgs = mutableListOf<MessageModel>()
    private lateinit var adapter: MessageChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_messaging)

        db = DatabaseHelper(this)

        // Get user and match info from Intent
        userId = intent.getIntExtra("USER_ID", -1)
        matchId = intent.getIntExtra("MATCH_ID", -1)
        otherId = intent.getIntExtra("OTHER_ID", -1)
        otherUsername = intent.getStringExtra("OTHER_NAME")

        lv = findViewById(R.id.lvMessages)
        etMessage = findViewById(R.id.etMessage)
        btnSend = findViewById(R.id.btnSend)
        tvChatHeader = findViewById(R.id.tvChatHeader)

        tvChatHeader.text = "Chat with ${otherUsername ?: "User"}"

        // initialize with MessageChatAdapter
        adapter = MessageChatAdapter(this, msgs, userId)
        lv.adapter = adapter

        loadMessages()

        btnSend.setOnClickListener {
            val text = etMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                // Save into DB
                db.sendMessage(matchId, userId, otherId, text)

                // Clear input box
                etMessage.setText("")

                // Reload chat
                loadMessages()
            }
        }
    }

    private fun loadMessages() {
        msgs.clear()
        val c: Cursor = db.getMessages(matchId)
        while (c.moveToNext()) {
            val id = c.getInt(c.getColumnIndexOrThrow("message_id"))
            val sender = c.getInt(c.getColumnIndexOrThrow("sender_id"))
            val content = c.getString(c.getColumnIndexOrThrow("message_content"))
            val sentAt = c.getString(c.getColumnIndexOrThrow("sent_at"))
            val isRead = c.getInt(c.getColumnIndexOrThrow("is_read")) == 1

            msgs.add(MessageModel(id, sender, content, sentAt, isRead))
        }
        c.close()

        adapter.notifyDataSetChanged()

        // Auto scroll to last message
        if (adapter.count > 0) {
            lv.post {
                lv.setSelection(adapter.count - 1)
            }
        }
    }
}