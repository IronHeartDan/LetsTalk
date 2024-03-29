package com.danapps.letstalk.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.danapps.letstalk.R
import com.danapps.letstalk.models.Contact
import kotlinx.android.synthetic.main.new_contact_item.view.*

class ContactsAdapter(val context: Context) :
    ListAdapter<Contact, ContactsAdapter.NewChatHolder>(DiffCallback()) {

    private lateinit var listener: NewChatClickListener

    private class DiffCallback : DiffUtil.ItemCallback<Contact>() {
        override fun areItemsTheSame(oldItem: Contact, newItem: Contact): Boolean {
            return oldItem.number === newItem.number
        }

        override fun areContentsTheSame(oldItem: Contact, newItem: Contact): Boolean {
            return oldItem.number == newItem.number
        }
    }

    inner class NewChatHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnClickListener {
                listener.onClick(getItem(adapterPosition))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewChatHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.new_contact_item, parent, false)
        return NewChatHolder(view)
    }

    override fun onBindViewHolder(holder: NewChatHolder, position: Int) {
        holder.itemView.new_contact_itemName.text = getItem(position).name
        holder.itemView.new_contact_itemNumber.text = getItem(position).number

        if (!getItem(position).profile_pic.equals("null")) {
            Glide.with(context).load(getItem(position).profile_pic).centerCrop()
                .into(holder.itemView.new_contact_profile_pic)
        } else {
            Glide.with(context).load(R.drawable.ic_account_circle).centerCrop()
                .into(holder.itemView.new_contact_profile_pic)
        }
    }

    interface NewChatClickListener {
        fun onClick(contact: Contact)
    }

    fun setNewChatClickListener(newChatClickListener: NewChatClickListener) {
        listener = newChatClickListener
    }

}