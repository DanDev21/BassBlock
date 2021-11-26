package com.dan.bassblock.feature_play_music.exoplayer

import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dan.bassblock.feature_play_music.domain.util.Constants
import com.dan.bassblock.feature_play_music.domain.util.Event
import com.dan.bassblock.feature_play_music.domain.util.Resource
import com.dan.bassblock.feature_play_music.service.exoplayer.MusicService

class MusicServiceConnection(
    context: Context
) {

    private val _connected = MutableLiveData<Event<Resource<Boolean>>>()
    val connected: LiveData<Event<Resource<Boolean>>> = _connected

    private val _networkError = MutableLiveData<Event<Resource<Boolean>>>()
    val networkError: LiveData<Event<Resource<Boolean>>> = _networkError

    private val _playbackState = MutableLiveData<PlaybackStateCompat?>()
    val playbackState: LiveData<PlaybackStateCompat?> = _playbackState

    private val _currentPlayingSong = MutableLiveData<MediaMetadataCompat?>()
    val currentPlayingSong: LiveData<MediaMetadataCompat?> = _currentPlayingSong

    lateinit var mediaController: MediaControllerCompat

    val transportControls: MediaControllerCompat.TransportControls
        get() = this.mediaController.transportControls

    private val mediaBrowserConnectionCallback = MediaBrowserConnectionCallback(context)

    private val mediaBrowser = MediaBrowserCompat(
        context,
        ComponentName(
            context,
            MusicService::class.java
        ),
        this.mediaBrowserConnectionCallback,
        null
    ).apply { connect() }

    fun subscribe(parentId: String, callback: MediaBrowserCompat.SubscriptionCallback) =
        this.mediaBrowser.subscribe(parentId, callback)

    fun unsubscribe(parentId: String, callback: MediaBrowserCompat.SubscriptionCallback) =
        this.mediaBrowser.unsubscribe(parentId, callback)

    private inner class MediaControllerCallback : MediaControllerCompat.Callback() {

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            _playbackState.postValue(state)
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            _currentPlayingSong.postValue(metadata)
        }

        override fun onSessionEvent(event: String?, extras: Bundle?) {
            super.onSessionEvent(event, extras)
            when (event) {
                Constants.ERROR_NETWORK -> _networkError.postValue(
                    Event(
                        Resource.error("Check your internet connection!")
                    )
                )
            }
        }

        override fun onSessionDestroyed() {
            mediaBrowserConnectionCallback.onConnectionSuspended()
        }
    }

    private inner class MediaBrowserConnectionCallback(
        private val context: Context
    ) : MediaBrowserCompat.ConnectionCallback() {

        override fun onConnected() {
            mediaController = MediaControllerCompat(context, mediaBrowser.sessionToken).apply {
                this.registerCallback(MediaControllerCallback())
            }
            _connected.postValue(Event(Resource.success(true)))
        }

        override fun onConnectionSuspended() {
            _connected.postValue(Event(Resource.error("suspended connection!", false)))
        }

        override fun onConnectionFailed() {
            _connected.postValue(Event(Resource.error("invalid connection!", false)))
        }
    }
}