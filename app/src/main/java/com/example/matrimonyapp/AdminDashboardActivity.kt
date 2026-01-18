package com.example.matrimonyapp

import android.database.Cursor
import android.os.Bundle
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.view.MenuItem
import android.widget.Button

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private lateinit var lv: ListView
    private val rows = mutableListOf<AdminUserRow>()
    private lateinit var adapter: AdminUserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)
        db = DatabaseHelper(this)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Admin Dashboard"

 //       val btnGoToRegister = findViewById<Button>(R.id.btnGoToRegister)
        lv = findViewById(R.id.lvUsers)
        adapter = AdminUserAdapter(this, rows, ::onVerify, ::onBlockToggle)
        lv.adapter = adapter
/*
        btnGoToRegister.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }*/

        findViewById<Button>(R.id.btnAdminLogout).setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        loadUsers()
        lv.setOnItemClickListener { _, _, pos, _ ->
            val row = rows[pos]
            val i = Intent(this, ProfileDetailsActivity::class.java).apply {
                putExtra("CURRENT_USER_ID", -1)   // no current user needed
                putExtra("VIEWED_USER_ID", row.userId)
                putExtra("MATCH_ID", -1)
                putExtra("IS_ADMIN", true)
            }
            startActivity(i)
        }

    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                val intent = Intent(this, LoginActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    private fun loadUsers() {
        rows.clear()
        val c: Cursor = db.getAllUsers()
        while (c.moveToNext()) {
            val uid = c.getInt(0)
            val username = c.getString(1)
            val email = c.getString(2)
            val isVerified = c.getInt(3) == 1
            val isBlocked = c.getInt(4) == 1
            rows.add(AdminUserRow(uid, username, email, isVerified, isBlocked))
        }
        c.close()
        adapter.notifyDataSetChanged()
    }

    private fun onVerify(userId: Int) {
        // Verify by username (need username); quick fetch:
        // Simpler: run SQL update by userId
        val username = rows.firstOrNull { it.userId == userId }?.username ?: return
        db.verifyUser(username)
        loadUsers()
    }

    private fun onBlockToggle(userId: Int, block: Boolean) {
        db.blockUser(userId, block)
        loadUsers()
    }

}


data class AdminUserRow(val userId: Int, val username: String, val email: String, val isVerified: Boolean, val isBlocked: Boolean)
