package com.danapps.letstalk.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.danapps.letstalk.R
import com.danapps.letstalk.models.Chats
import com.danapps.letstalk.models.Contact
import kotlinx.android.synthetic.main.item_chat_layout.view.*

class ChatsAdapter : ListAdapter<Chats, ChatsAdapter.ChatHolder>(ChatsDiff()) {

    private lateinit var chatclickListener: ChatclickListener

    class ChatsDiff : DiffUtil.ItemCallback<Chats>() {
        override fun areItemsTheSame(oldItem: Chats, newItem: Chats): Boolean {
            return oldItem.who == newItem.who
        }

        override fun areContentsTheSame(oldItem: Chats, newItem: Chats): Boolean {
            return oldItem == newItem
        }

    }

    inner class ChatHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnClickListener {
                val item = getItem(adapterPosition)
                chatclickListener.onClick(Contact(item.name, item.profile_pic, item.who))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_chat_layout, parent, false)
        return ChatHolder(view)
    }

    override fun onBindViewHolder(holder: ChatHolder, position: Int) {
        holder.itemView.chat_item_name.text = getItem(position).name
        holder.itemView.chat_item_msg.text = getItem(position).msg
    }

    interface ChatclickListener {
        fun onClick(contact: Contact)
    }

    fun setOnChatClickListener(chatclickListener: ChatclickListener) {
        this.chatclickListener = chatclickListener
    }
}