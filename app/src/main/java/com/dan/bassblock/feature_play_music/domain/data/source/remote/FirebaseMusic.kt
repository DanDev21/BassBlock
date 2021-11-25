package com.dan.bassblock.feature_play_music.domain.data.source.remote

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import androidx.core.net.toUri
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FirebaseMusic @Inject constructor(
    private val songDatabase: SongDatabase
){

    var songs = emptyList<MediaMetadataCompat>()

    private val onReadyListeners = mutableListOf<(Boolean) -> Unit>()

    private var state: State = State.CREATED
        set(value) {
            if (value == State.LOADED || value == State.ERROR) {
                synchronized(onReadyListeners) {
                    field = value
                    this.onReadyListeners.forEach { listener ->
                        listener(state == State.LOADED)
                    }
                }
            } else {
                field = value
            }
        }

    fun addOnReadyListener(routine: (Boolean) -> Unit): Boolean {
        return if (this.state == State.CREATED || this.state == State.LOADING) {
            this.onReadyListeners += routine; true
        } else {
            routine(this.state == State.LOADED); false
        }
    }

    suspend fun fetchMediaData() = withContext(Dispatchers.IO) {
        state = State.LOADING
        songs = songDatabase.getSongs().map { song ->
            MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, song.id)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.title)
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, song.title)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, song.artist)
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, song.artist)
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION, song.artist)
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, song.songUrl)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, song.imageUrl)
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI, song.imageUrl)
                .build()
        }
        state = State.LOADED
    }

    fun getMediaSource(dataSourceFactory: DefaultDataSource.Factory): ConcatenatingMediaSource =
        ConcatenatingMediaSource().apply {
            songs.forEach { song ->
                val mediaItem = MediaItem
                    .fromUri(song.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI))
                val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(mediaItem)
                this.addMediaSource(mediaSource)
            }
        }

    fun getMediaItems() = this.songs.map { song ->
        val description = MediaDescriptionCompat.Builder()
            .setTitle(song.description.title)
            .setSubtitle(song.description.subtitle)
            .setMediaId(song.description.mediaId)
            .setIconUri(song.description.iconUri)
            .setMediaUri(song.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI).toUri())
            .build()
        MediaBrowserCompat.MediaItem(description, MediaBrowserCompat.MediaItem.FLAG_PLAYABLE)
    }.toMutableList()
}

enum class State {
    CREATED,
    LOADING,
    LOADED,
    ERROR
}