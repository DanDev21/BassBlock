package com.dan.bassblock.feature_play_music.service.exoplayer

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.media.MediaBrowserServiceCompat
import com.dan.bassblock.feature_play_music.domain.data.source.remote.FirebaseMusic
import com.dan.bassblock.feature_play_music.domain.util.Constants
import com.dan.bassblock.feature_play_music.exoplayer.MusicNotificationManager
import com.dan.bassblock.feature_play_music.exoplayer.MusicPlaybackPreparer
import com.dan.bassblock.feature_play_music.exoplayer.callback.MusicPlayerNotificationListener
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.upstream.DefaultDataSource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

private const val TAG = ""

@AndroidEntryPoint
class MusicService : MediaBrowserServiceCompat() {

    companion object {
        var currentSongDuration = 0L
            private set
    }

    @Inject
    lateinit var exoplayer: ExoPlayer

    @Inject
    lateinit var datasourceFactory: DefaultDataSource.Factory

    @Inject
    lateinit var firebaseMusic: FirebaseMusic

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Main + this.job)

    var isForegroundService = false
    private var currentPlayingSong: MediaMetadataCompat? = null

    private var isPlayerInitialized = false
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaSessionConnector: MediaSessionConnector
    private lateinit var musicNotificationManager: MusicNotificationManager

    override fun onCreate() {
        super.onCreate()

        scope.launch {
            firebaseMusic.fetchMediaData()
        }
        this.initSession()
        this.initNotificationManager()

    }

    private fun initSession() {
        val activityIntent = this.getActivityIntent()
        this.mediaSession = MediaSessionCompat(this, TAG).apply {
            this.setSessionActivity(activityIntent)
            this.isActive = true
        }
        this.sessionToken = this.mediaSession.sessionToken
        this.mediaSessionConnector.setPlayer(exoplayer)
        this.mediaSessionConnector.setPlaybackPreparer(MusicPlaybackPreparer(firebaseMusic) {
            this.currentPlayingSong = it
            this.preparePlayer(firebaseMusic.songs, it, true)
        })
        this.mediaSessionConnector.setQueueNavigator(MusicQueueNavigator())
    }

    private fun getActivityIntent(): PendingIntent? =
        this.packageManager?.getLaunchIntentForPackage(packageName)?.let {
            PendingIntent.getActivity(this, 0, it, PendingIntent.FLAG_IMMUTABLE)
        }

    private fun initNotificationManager() {
        this.musicNotificationManager = MusicNotificationManager(
            this,
            mediaSession.sessionToken,
            MusicPlayerNotificationListener(this)
        ) {
            currentSongDuration = exoplayer.duration
        }.apply { showNotification(exoplayer) }
    }

    private fun preparePlayer(
        songs: List<MediaMetadataCompat>,
        song: MediaMetadataCompat?,
        play: Boolean = false
    ) {
        val songIndex = if (this.currentPlayingSong == null) 0 else songs.indexOf(song)
        this.exoplayer.apply {
            setMediaSource(firebaseMusic.getMediaSource(datasourceFactory))
            prepare()
            seekTo(songIndex, 0L)
            playWhenReady = play
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        this.exoplayer.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        this.scope.cancel()
        this.exoplayer.release()
    }

    fun start(notification: Notification) {
        this.startForeground(Constants.NOTIFICATION_ID, notification)
        this.isForegroundService = true
    }

    fun stop() {
        this.stopForeground(true)
        this.isForegroundService = false
        this.stopSelf()
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ) = BrowserRoot(Constants.MEDIA_ROOT_ID, null)

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        when (parentId) {
            Constants.MEDIA_ROOT_ID -> {
                val resultSent = this.firebaseMusic.addOnReadyListener { initialized ->
                    if (initialized) {
                        result.sendResult(firebaseMusic.getMediaItems())
                        if (!isPlayerInitialized && firebaseMusic.songs.isNotEmpty()) {
                            this.preparePlayer(firebaseMusic.songs, firebaseMusic.songs[0])
                            this.isPlayerInitialized = true
                        }
                    } else {
                        result.sendResult(null)
                    }
                }

                if (!resultSent) {
                    result.detach()
                }
            }
        }
    }

    private inner class MusicQueueNavigator : TimelineQueueNavigator(mediaSession) {

        override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat =
            firebaseMusic.songs[windowIndex].description
    }
}