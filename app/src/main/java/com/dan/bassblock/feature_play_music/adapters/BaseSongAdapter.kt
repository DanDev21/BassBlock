package com.dan.bassblock.feature_play_music.adapters

import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.dan.bassblock.feature_play_music.domain.data.model.Song

abstract class BaseSongAdapter<T : RecyclerView.ViewHolder> :
    RecyclerView.Adapter<T>() {

    protected val diffCallback = object : DiffUtil.ItemCallback<Song>() {

        override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    protected abstract val differ: AsyncListDiffer<Song>

    var songs: List<Song>
        get() = this.differ.currentList
        set(value) = this.differ.submitList(value)

    protected var onClickListener: ((Song) -> Unit)? = null

    override fun getItemCount() = songs.size

    fun setClickListener(listener: (Song) -> Unit) {
        this.onClickListener = listener
    }
}