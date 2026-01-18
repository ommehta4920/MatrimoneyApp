package com.example.matrimonyapp
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import android.content.Intent
import java.util.*


class SendMessageActivity : AppCompatActivity() {
    private lateinit var db: DatabaseHelper
    private var matchId: Int = -1
    private var userId: Int = -1
    private var otherId: Int = -1
    private lateinit var adapter: ArrayAdapter<String>
    private val messages = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_message)
        db = DatabaseHelper(this)

        matchId = intent.getIntExtra("MATCH_ID", -1)
        userId = intent.getIntExtra("USER_ID", -1)
        otherId = intent.getIntExtra("OTHER_ID", -1)

        val lv = findViewById<ListView>(R.id.lvMessages)
        val et = findViewById<EditText>(R.id.etMessage)
        val btn = findViewById<Button>(R.id.btnSend)

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, messages)
        lv.adapter = adapter

        loadMessages()

        btn.setOnClickListener {
            val txt = et.text.toString()
            if (txt.isNotBlank()) {
                db.sendMessage(matchId, userId, otherId, txt)
                et.text.clear()
                loadMessages()
            }
        }
    }

    private fun loadMessages() {
        messages.clear()
        val c = db.getMessages(matchId)
        while (c.moveToNext()) {
            messages.add(c.getString(c.getColumnIndexOrThrow("message_content")))
        }
        c.close()
        adapter.notifyDataSetChanged()
    }
}
