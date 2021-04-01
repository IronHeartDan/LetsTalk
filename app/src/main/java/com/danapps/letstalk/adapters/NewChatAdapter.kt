package com.danapps.letstalk.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.danapps.letstalk.R
import com.danapps.letstalk.models.Contact
import kotlinx.android.synthetic.main.new_chat_item.view.*

class NewChatAdapter :
    ListAdapter<Contact, NewChatAdapter.NewChatHolder>(DiffCallback()) {


    private class DiffCallback : DiffUtil.ItemCallback<Contact>() {
        override fun areItemsTheSame(oldItem: Contact, newItem: Contact): Boolean {
            return oldItem.number === newItem.number
        }

        override fun areContentsTheSame(oldItem: Contact, newItem: Contact): Boolean {
            return oldItem.number == newItem.number
        }
    }

    class NewChatHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewChatHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.new_chat_item, parent, false)
        return NewChatHolder(view)
    }

    override fun onBindViewHolder(holder: NewChatHolder, position: Int) {
        holder.itemView.new_chat_itemName.text = getItem(position).name
        holder.itemView.new_chat_itemNumber.text = getItem(position).number
    }
}