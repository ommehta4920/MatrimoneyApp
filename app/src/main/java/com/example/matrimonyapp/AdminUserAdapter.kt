package com.example.matrimonyapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.TextView

class AdminUserAdapter(
    private val ctx: Context,
    private val data: List<AdminUserRow>,
    private val onVerify: (Int) -> Unit,
    private val onBlockToggle: (Int, Boolean) -> Unit
) : BaseAdapter() {
    override fun getCount(): Int = data.size
    override fun getItem(position: Int): Any = data[position]
    override fun getItemId(position: Int): Long = data[position].userId.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val v = convertView ?: LayoutInflater.from(ctx).inflate(R.layout.row_user_admin, parent, false)
        val tvUsername = v.findViewById<TextView>(R.id.tvUsername)
        val tvEmail = v.findViewById<TextView>(R.id.tvEmail)
        val btnVerify = v.findViewById<Button>(R.id.btnVerify)
        val btnBlock = v.findViewById<Button>(R.id.btnBlock)

        val row = data[position]
        tvUsername.text = "${row.username} ${if (row.isVerified) "✓" else "✗"}"
        tvEmail.text = row.email

        btnVerify.isEnabled = !row.isVerified
        btnVerify.setOnClickListener { onVerify(row.userId) }

        btnBlock.text = if (row.isBlocked) "Unblock" else "Block"
        btnBlock.setOnClickListener { onBlockToggle(row.userId, !row.isBlocked) }

        return v
    }
}
