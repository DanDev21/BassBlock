package com.dan.bassblock.feature_play_music.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.dan.bassblock.R
import com.dan.bassblock.databinding.LayoutSwipeSongBinding

class SwipeSongAdapter : BaseSongAdapter<SwipeSongAdapter.SwipeSongViewHolder>() {

    override val differ = AsyncListDiffer(this, diffCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SwipeSongViewHolder {
        return SwipeSongViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.layout_swipe_song,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: SwipeSongViewHolder, position: Int) {
        val song = this.songs[position]
        holder.apply {
            val text = "${song.title} - ${song.artist}"
            this.binding.textViewTitle.text = text
            this.itemView.setOnClickListener {
                onClickListener?.let { it(song) }
            }
        }
    }

    class SwipeSongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val binding = LayoutSwipeSongBinding.bind(itemView)
    }
}