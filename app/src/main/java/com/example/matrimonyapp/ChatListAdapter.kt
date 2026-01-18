package com.example.matrimonyapp

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView

class ChatListAdapter(
    private val ctx: Context,
    private val data: ArrayList<HashMap<String, String>>
) : BaseAdapter() {

    override fun getCount(): Int = data.size
    override fun getItem(position: Int): Any = data[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val v = convertView ?: LayoutInflater.from(ctx).inflate(R.layout.row_chat, parent, false)

        val otherUser = v.findViewById<TextView>(R.id.tvOtherUser)
        val lastMessage = v.findViewById<TextView>(R.id.tvLastMessage)
        val profilePic = v.findViewById<ImageView>(R.id.ivProfilePic)

        val item = data[position]
        otherUser.text = item["otherUser"]
        lastMessage.text = item["lastMessage"]

        val pic = item["profilePic"]

        if (!pic.isNullOrBlank()) {
            try {
                val uri = Uri.parse(pic)
                profilePic.setImageURI(uri)
            } catch (e: Exception) {
                e.printStackTrace()
                profilePic.setImageResource(R.drawable.img) // fallback
            }
        } else {
            profilePic.setImageResource(R.drawable.img) // fallback
        }

        return v
    }

}
