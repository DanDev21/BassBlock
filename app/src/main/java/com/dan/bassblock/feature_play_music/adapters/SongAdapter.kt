package com.dan.bassblock.feature_play_music.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.dan.bassblock.R
import com.dan.bassblock.databinding.LayoutSongBinding
import javax.inject.Inject

class SongAdapter @Inject constructor(
    private val glide: RequestManager
) : BaseSongAdapter<SongAdapter.SongViewHolder>() {

    override val differ = AsyncListDiffer(this, diffCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        return SongViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.layout_song,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holderBase: SongViewHolder, position: Int) {
        val song = this.songs[position]
        holderBase.apply {
            this.binding.textViewTitle.text = song.title
            this.binding.textViewArtist.text = song.artist
            glide.load(song.imageUrl)
                .centerCrop()
                .into(binding.imageView)
            this.itemView.setOnClickListener {
                onClickListener?.let { it(song) }
            }
        }
    }

    class SongViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        val binding = LayoutSongBinding.bind(itemView)
    }
}