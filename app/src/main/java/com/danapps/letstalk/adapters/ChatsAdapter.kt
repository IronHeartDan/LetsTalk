package com.danapps.letstalk.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.danapps.letstalk.R
import com.danapps.letstalk.models.Chats
import com.danapps.letstalk.models.Contact
import kotlinx.android.synthetic.main.item_chat_layout.view.*
import kotlinx.android.synthetic.main.new_contact_item.view.*

class ChatsAdapter(val context: Context) :
    ListAdapter<Chats, ChatsAdapter.ChatHolder>(ChatsDiff()) {

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
                chatclickListener.onClick(Contact(item.name!!, item.profile_pic, item.who))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_chat_layout, parent, false)
        return ChatHolder(view)
    }

    override fun onBindViewHolder(holder: ChatHolder, position: Int) {

        if(getItem(position).name == null){
            getItem(position).name = getItem(position).who
        }

        if(getItem(position).profile_pic.equals("null")){
            Glide.with(context).load(getItem(position).profile_pic).centerCrop().into(holder.itemView.chat_item_profile_pic)
        }else{
            Glide.with(context).load(R.drawable.ic_account_circle).centerCrop().into(holder.itemView.chat_item_profile_pic)
        }

        holder.itemView.chat_item_name.text = getItem(position).name
        holder.itemView.chat_item_msg.text = getItem(position).msg


        if (getItem(position).msgStats != null) {
            Log.d("TEST", "onBindViewHolder: ")
            holder.itemView.chat_msgStats.visibility = View.VISIBLE
            when (getItem(position)!!.msgStats) {
                1 -> holder.itemView.chat_msgStats?.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.ic_msg_to_server
                    )
                )
                2 -> holder.itemView.chat_msgStats?.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.ic_msg_to_user
                    )
                )
                3 -> holder.itemView.chat_msgStats?.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.ic_msg_seen
                    )
                )
                else -> holder.itemView.chat_msgStats?.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.ic_msg_sending
                    )
                )
            }
        } else {
            holder.itemView.chat_msgStats.visibility = View.GONE
        }

    }

    interface ChatclickListener {
        fun onClick(contact: Contact)
    }

    fun setOnChatClickListener(chatclickListener: ChatclickListener) {
        this.chatclickListener = chatclickListener
    }
}