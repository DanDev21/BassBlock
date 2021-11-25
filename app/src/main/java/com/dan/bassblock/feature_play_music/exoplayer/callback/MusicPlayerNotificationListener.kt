package com.dan.bassblock.feature_play_music.exoplayer.callback

import android.app.Notification
import android.content.Intent
import androidx.core.content.ContextCompat
import com.dan.bassblock.feature_play_music.service.exoplayer.MusicService
import com.google.android.exoplayer2.ui.PlayerNotificationManager

class MusicPlayerNotificationListener (
    private val musicService: MusicService
) : PlayerNotificationManager.NotificationListener {

    override fun onNotificationCancelled(notificationId: Int, dismissed: Boolean) {
        super.onNotificationCancelled(notificationId, dismissed)
        this.musicService.stop()
    }

    override fun onNotificationPosted(
        notificationId: Int,
        notification: Notification,
        ongoing: Boolean
    ) {
        super.onNotificationPosted(notificationId, notification, ongoing)
        if (ongoing && !this.musicService.isForegroundService) {
            this.startForegroundService(notification)
        }
    }

    private fun startForegroundService(notification: Notification) {
        ContextCompat.startForegroundService(
            musicService,
            Intent(musicService.applicationContext, musicService::class.java)
        )
        this.musicService.start(notification)
    }
}