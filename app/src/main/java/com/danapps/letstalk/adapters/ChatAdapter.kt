package com.danapps.letstalk.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.danapps.letstalk.R
import com.danapps.letstalk.models.ChatMessage
import kotlinx.android.synthetic.main.chat_item_left.view.*
import kotlinx.android.synthetic.main.chat_item_right.view.*

class ChatAdapter(val number: String) :
    ListAdapter<ChatMessage, ChatAdapter.ChatHolder>(ChatDiffUtil()) {


    class ChatDiffUtil : DiffUtil.ItemCallback<ChatMessage>() {
        override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem == newItem
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
            else -> null
        }

        return ChatHolder(view!!)
    }

    override fun onBindViewHolder(holder: ChatHolder, position: Int) {
        if (getItem(position).from == number) {
            holder.itemView.chat_message_right.text = getItem(position).msg
        } else {
            holder.itemView.chat_message_left.text = getItem(position).msg
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).from == number) {
            0
        } else {
            1
        }
    }

}