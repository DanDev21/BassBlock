package com.dan.bassblock.feature_play_music.exoplayer.callback

import android.widget.Toast
import com.dan.bassblock.feature_play_music.service.exoplayer.MusicService
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player

class MusicPlayerEventListener(
    private val musicService: MusicService
) : Player.Listener {

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        super.onPlaybackStateChanged(playbackState)
        if (playbackState == Player.STATE_READY && !playWhenReady) {
            this.musicService.stopForeground(false)
        }
    }

    override fun onPlayerError(error: PlaybackException) {
        super.onPlayerError(error)
        Toast.makeText(musicService, error.message, Toast.LENGTH_LONG).show()
    }
}