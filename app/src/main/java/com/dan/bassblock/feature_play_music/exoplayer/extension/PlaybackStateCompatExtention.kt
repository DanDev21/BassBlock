package com.dan.bassblock.feature_play_music.exoplayer.extension

import android.media.session.PlaybackState
import android.os.SystemClock
import android.support.v4.media.session.PlaybackStateCompat

inline val PlaybackStateCompat.isPrepared
    get() = this.state == PlaybackStateCompat.STATE_BUFFERING ||
            this.state == PlaybackStateCompat.STATE_PLAYING ||
            this.state == PlaybackStateCompat.STATE_PAUSED

inline val PlaybackStateCompat.isPlaying
    get() = this.state == PlaybackStateCompat.STATE_BUFFERING ||
            this.state == PlaybackStateCompat.STATE_PLAYING

inline val PlaybackStateCompat.isPlayEnabled
    get() = (this.actions and PlaybackStateCompat.ACTION_PLAY) != 0L ||
            ((this.actions and PlaybackStateCompat.ACTION_PLAY_PAUSE) != 0L &&
                    (this.state == PlaybackState.STATE_PAUSED))

inline val PlaybackStateCompat.currentPlaybackPosition: Long
    get() = if (this.state == PlaybackStateCompat.STATE_PLAYING) {
        val timeDelta = SystemClock.elapsedRealtime() - this.lastPositionUpdateTime
        (this.position + (timeDelta * this.playbackSpeed)).toLong()
    } else this.position