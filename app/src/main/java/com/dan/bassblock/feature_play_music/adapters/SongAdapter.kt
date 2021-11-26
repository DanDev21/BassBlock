package com.dan.bassblock.feature_play_music.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.dan.bassblock.R
import com.dan.bassblock.databinding.LayoutSongBinding
import com.dan.bassblock.feature_play_music.domain.data.model.Song
import javax.inject.Inject

class SongAdapter @Inject constructor(
    private val glide: RequestManager
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    private val diffCallback = object : DiffUtil.ItemCallback<Song>() {

        override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    private val differ = AsyncListDiffer(this, this.diffCallback)

    var songs: List<Song>
        get() = this.differ.currentList
        set(value) = this.differ.submitList(value)

    private var onClickListener: ((Song) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        return SongViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.layout_song,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = this.songs[position]
        holder.apply {
            this.binding.textViewTitle.text = song.title
            this.binding.textViewArtist.text = song.artist
            glide.load(song.imageUrl).into(binding.imageView)
            this.itemView.setOnClickListener {
                onClickListener?.let { it(song) }
            }
        }
    }

    override fun getItemCount() = songs.size

    fun setOnClickListener(listener: (Song) -> Unit) {
        this.onClickListener = listener
    }

    class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val binding = LayoutSongBinding.bind(itemView)
    }
}