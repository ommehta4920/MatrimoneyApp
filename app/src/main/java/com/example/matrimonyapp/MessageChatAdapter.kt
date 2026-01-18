import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.matrimonyapp.MessageModel
import com.example.matrimonyapp.R

class MessageChatAdapter(
    private val context: Context,
    private val messages: List<MessageModel>,
    private val currentUserId: Int
) : BaseAdapter() {

    override fun getCount(): Int = messages.size
    override fun getItem(position: Int): Any = messages[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getViewTypeCount(): Int = 2
    override fun getItemViewType(position: Int): Int {
        val msg = messages[position]
        return if (msg.senderId == currentUserId) 0 else 1
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val msg = messages[position]
        val viewType = getItemViewType(position)

        // Always inflate new view based on viewType
        val view = LayoutInflater.from(context).inflate(
            if (viewType == 0) R.layout.row_message_me else R.layout.row_message_other,
            parent,
            false
        )

        val textView: TextView = if (viewType == 0)
            view.findViewById(R.id.tvMessageMe)
        else
            view.findViewById(R.id.tvMessageOther)

        textView.text = msg.content
        return view
    }
}
