package com.example.matrimonyapp

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView

class MatchAdapter(
    private val ctx: Context,
    private var data: ArrayList<MatchRow>
) : BaseAdapter() {

    override fun getCount(): Int = data.size
    override fun getItem(position: Int): Any = data[position]
    override fun getItemId(position: Int): Long = data[position].userId.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val v = convertView ?: LayoutInflater.from(ctx).inflate(R.layout.row_match, parent, false)

        val tvTitle = v.findViewById<TextView>(R.id.tvTitle)   // <-- FIXED
        val tvSubtitle = v.findViewById<TextView>(R.id.tvSubtitle)
        val ivProfilePic = v.findViewById<ImageView>(R.id.ivProfilePic)

        val item = data[position]
        tvTitle.text = item.title   // <-- FIXED
        tvSubtitle.text = item.subtitle

        val pic = item.imagePath
        if (!pic.isNullOrBlank()) {
            try {
                val uri = Uri.parse(pic)
                ivProfilePic.setImageURI(uri)
            } catch (e: Exception) {
                ivProfilePic.setImageResource(R.drawable.img) // fallback
            }
        } else {
            ivProfilePic.setImageResource(R.drawable.img) // fallback
        }

        return v
    }

    //  Add this method
    fun updateData(newData: ArrayList<MatchRow>) {
        data.clear()
        data.addAll(newData)
        notifyDataSetChanged()
    }
}

