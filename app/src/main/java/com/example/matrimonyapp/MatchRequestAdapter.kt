package com.example.matrimonyapp

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import java.lang.Exception

class MatchRequestAdapter(
    private val context: Context,
    private val requests: ArrayList<MatchRequest>,
    private val db: DatabaseHelper,
    private val currentUserId: Int,
    private val listener: OnRequestActionListener
) : BaseAdapter() {

    interface OnRequestActionListener {
        fun onActionDone()
    }

    override fun getCount(): Int = requests.size
    override fun getItem(position: Int): Any = requests[position]
    override fun getItemId(position: Int): Long = requests[position].matchId.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val request = requests[position]
        val inflater = LayoutInflater.from(context)
        val view = convertView ?: inflater.inflate(R.layout.row_requests, parent, false)

        val imgProfile = view.findViewById<ImageView>(R.id.imgProfile)
        val tvMessage = view.findViewById<TextView>(R.id.tvMessage)
        val btnAccept = view.findViewById<Button>(R.id.btnAccept)
        val btnReject = view.findViewById<Button>(R.id.btnReject)

        // Load profile image
        if (!request.profilePic.isNullOrBlank()) {
            try {
                val uri = Uri.parse(request.profilePic)
                imgProfile.setImageURI(uri)

            } catch (e: Exception) {
                imgProfile.setImageResource(R.drawable.img)
            }
        } else {
            imgProfile.setImageResource(R.drawable.img)
        }

        // Status handling
        when (request.status) {
            "Pending" -> {
                if (request.senderId == currentUserId) {
                    tvMessage.text = "Your request has been sent to ${request.username}"
                    btnAccept.visibility = View.GONE
                    btnReject.visibility = View.GONE
                } else {
                    tvMessage.text = "${request.username} wants to interact with you"
                    btnAccept.visibility = View.VISIBLE
                    btnReject.visibility = View.VISIBLE

                    btnAccept.setOnClickListener {
                        db.setMatchStatus(request.matchId, "Accepted")
                        Toast.makeText(context, "You accepted ${request.username}", Toast.LENGTH_SHORT).show()
                        listener.onActionDone() // reload via Activity
                    }

                    btnReject.setOnClickListener {
                        db.setMatchStatus(request.matchId, "Rejected")
                        Toast.makeText(context, "You rejected ${request.username}", Toast.LENGTH_SHORT).show()
                        listener.onActionDone() // reload via Activity
                    }
                }
            }
            "Accepted" -> {
                tvMessage.text = "You and ${request.username} are connected"
                btnAccept.visibility = View.GONE
                btnReject.visibility = View.GONE
            }
            "Rejected" -> {
                if (currentUserId == request.senderId) {
                    // I was the sender, so the other user rejected me
                    tvMessage.text = "${request.username} rejected your request"
                } else {
                    // I was the receiver, so I rejected them
                    tvMessage.text = "You rejected ${request.username}'s request"
                }
                btnAccept.visibility = View.GONE
                btnReject.visibility = View.GONE
            }
        }

        return view
    }

    fun updateData(newList: List<MatchRequest>) {
        requests.clear()
        requests.addAll(newList)
        notifyDataSetChanged()
    }
}
