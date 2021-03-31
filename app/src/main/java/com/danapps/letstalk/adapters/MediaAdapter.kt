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
import com.danapps.letstalk.models.Media
import kotlinx.android.synthetic.main.gallery_item.view.*

class MediaAdapter(val context: Context) :
    ListAdapter<Media, MediaAdapter.MediaViewHolder>(DiffCallback()) {

    private lateinit var listener: OnItemClickListener


    private class DiffCallback : DiffUtil.ItemCallback<Media>() {
        override fun areItemsTheSame(oldItem: Media, newItem: Media): Boolean {
            return oldItem.uri === newItem.uri
        }

        override fun areContentsTheSame(oldItem: Media, newItem: Media): Boolean {
            return oldItem.uri == newItem.uri
        }
    }

    inner class MediaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                listener.onItemClick(getItem(position))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.gallery_item, parent, false)
        return MediaViewHolder(view)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        Glide.with(context).load(getItem(position).uri).into(holder.itemView.item_gallery)
    }

    interface OnItemClickListener {
        fun onItemClick(media: Media)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

}