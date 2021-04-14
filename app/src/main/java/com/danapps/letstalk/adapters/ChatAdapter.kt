package com.danapps.letstalk.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.danapps.letstalk.ChatActivity
import com.danapps.letstalk.R
import com.danapps.letstalk.models.ChatMessage
import kotlinx.android.synthetic.main.chat_item_left.view.*
import kotlinx.android.synthetic.main.chat_item_right.view.*

class ChatAdapter(val context: Context, val number: String) :
    PagedListAdapter<ChatMessage, ChatAdapter.ChatHolder>(ChatDiffUtil(context, number)) {

    class ChatDiffUtil(val context: Context, val number: String) :
        DiffUtil.ItemCallback<ChatMessage>() {
        override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            val check = oldItem.id == newItem.id
            Log.d("TEST", "areItemsTheSame: $check")
            if (!check && newItem.to == number) {
                (context as ChatActivity).markSeen()
                Log.d("TEST", "areItemsTheSame: Mark Seen")
            }
            return check
        }

        override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem.msgStats == newItem.msgStats
        }

    }

    class ChatHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatHolder {
        val view = when (viewType) {
            0 -> {
                LayoutInflater.from(parent.context).inflate(R.layout.chat_item_right, parent, false)
            }
            1 -> {
                LayoutInflater.from(parent.context).inflate(R.layout.chat_item_left, parent, false)
            }
            2 -> {
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.chat_item_place_holder, parent, false)
            }
            else -> null
        }

        return ChatHolder(view!!)
    }

    override fun onBindViewHolder(holder: ChatHolder, position: Int) {
        if (getItem(position) != null) {
            if (getItem(position)!!.from == number) {
                holder.itemView.chat_message_right.text = getItem(position)!!.msg
                when (getItem(position)!!.msgStats) {
                    1 -> holder.itemView.msgStats?.setImageDrawable(
                        ContextCompat.getDrawable(
                            context,
                            R.drawable.ic_msg_to_server
                        )
                    )
                    2 -> holder.itemView.msgStats?.setImageDrawable(
                        ContextCompat.getDrawable(
                            context,
                            R.drawable.ic_msg_to_user
                        )
                    )
                    3 -> holder.itemView.msgStats?.setImageDrawable(
                        ContextCompat.getDrawable(
                            context,
                            R.drawable.ic_msg_seen
                        )
                    )
                    else -> holder.itemView.msgStats?.setImageDrawable(
                        ContextCompat.getDrawable(
                            context,
                            R.drawable.ic_msg_sending
                        )
                    )
                }
            } else {
                holder.itemView.chat_message_left.text = getItem(position)!!.msg
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position) != null) {
            if (getItem(position)!!.from == number) {
                0
            } else {
                1
            }
        } else {
            2
        }
    }
}