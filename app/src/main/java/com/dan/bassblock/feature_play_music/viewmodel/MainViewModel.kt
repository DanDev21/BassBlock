package com.dan.bassblock.feature_play_music.viewmodel

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dan.bassblock.feature_play_music.domain.data.model.Song
import com.dan.bassblock.feature_play_music.domain.util.Constants
import com.dan.bassblock.feature_play_music.domain.util.Resource
import com.dan.bassblock.feature_play_music.exoplayer.MusicServiceConnection
import com.dan.bassblock.feature_play_music.exoplayer.isPlayEnabled
import com.dan.bassblock.feature_play_music.exoplayer.isPlaying
import com.dan.bassblock.feature_play_music.exoplayer.isPrepared
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val musicServiceConnection: MusicServiceConnection
) : ViewModel() {
    private val _mediaItems = MutableLiveData<Resource<List<Song>>>()
    val mediaItems: LiveData<Resource<List<Song>>> = _mediaItems

    val connected = this.musicServiceConnection.connected
    val networkError = this.musicServiceConnection.networkError
    val currentPlayingSong = this.musicServiceConnection.currentPlayingSong
    val playbackState = this.musicServiceConnection.playbackState

    init {
        this._mediaItems.postValue(Resource.loading())
        this.musicServiceConnection.subscribe(Constants.MEDIA_ROOT_ID, object : MediaBrowserCompat.SubscriptionCallback() {

            override fun onChildrenLoaded(
                parentId: String,
                children: MutableList<MediaBrowserCompat.MediaItem>
            ) {
                super.onChildrenLoaded(parentId, children)
                val items = children.map {
                    Song(
                        it.mediaId!!,
                        it.description.title.toString(),
                        it.description.subtitle.toString(),
                        it.description.mediaUri.toString(),
                        it.description.iconUri.toString()
                    )
                }
                _mediaItems.postValue(Resource.success(items))
            }
        })
    }

    fun skipToNextSong() {
        this.musicServiceConnection.transportControls.skipToNext()
    }

    fun skipToPrevious() {
        this.musicServiceConnection.transportControls.skipToPrevious()
    }

    fun seekTo(position: Long) {
        this.musicServiceConnection.transportControls.seekTo(position)
    }

    fun toggleSong(song: Song, toggle: Boolean = false) {
        val prepared = this.playbackState.value?.isPrepared ?: false
        if (prepared && song.id ==
            this.currentPlayingSong.value?.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)) {
            this.playbackState.value?.let { playbackState ->
                when {
                    playbackState.isPlaying -> if (toggle) this.musicServiceConnection.transportControls.pause()
                    playbackState.isPlayEnabled -> this.musicServiceConnection.transportControls.play()
                    else -> Unit
                }
            }
        } else {
            this.musicServiceConnection.transportControls.playFromMediaId(song.id, null)
        }
    }

    override fun onCleared() {
        super.onCleared()
        this.musicServiceConnection.unsubscribe(Constants.MEDIA_ROOT_ID, object : MediaBrowserCompat.SubscriptionCallback() {})
    }
}