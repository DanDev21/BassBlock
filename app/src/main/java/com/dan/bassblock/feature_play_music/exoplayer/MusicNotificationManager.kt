package com.dan.bassblock.feature_play_music.exoplayer

import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.dan.bassblock.R
import com.dan.bassblock.feature_play_music.domain.util.Constants
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager

class MusicNotificationManager(
    private val context: Context,
    sessionToken: MediaSessionCompat.Token,
    notificationListener: PlayerNotificationManager.NotificationListener,
    private val newSongCallback: () -> Unit
) {

    private val notificationManager: PlayerNotificationManager

    init {
        val mediaController = MediaControllerCompat(context, sessionToken)
        val descriptionAdapter = DescriptionAdapter(mediaController)
        this.notificationManager = PlayerNotificationManager.Builder(
            context,
            Constants.NOTIFICATION_ID,
            Constants.NOTIFICATION_CHANNEL_ID
        )
            .setChannelNameResourceId(R.string.notification_channel_name)
            .setChannelDescriptionResourceId(R.string.notification_channel_description)
            .setSmallIconResourceId(R.drawable.img_neon_musical_note)
            .setMediaDescriptionAdapter(descriptionAdapter)
            .setNotificationListener(notificationListener)
            .build()
    }

    fun showNotification(player: Player) = this.notificationManager.setPlayer(player)

    private inner class DescriptionAdapter(
        private val mediaController: MediaControllerCompat
    ) : PlayerNotificationManager.MediaDescriptionAdapter {

        override fun getCurrentContentTitle(player: Player): CharSequence =
            this.mediaController.metadata.description.title.toString()

        override fun createCurrentContentIntent(player: Player): PendingIntent? =
            this.mediaController.sessionActivity

        override fun getCurrentContentText(player: Player): CharSequence =
            this.mediaController.metadata.description.subtitle.toString()

        override fun getCurrentLargeIcon(
            player: Player,
            callback: PlayerNotificationManager.BitmapCallback
        ): Bitmap? {
            Glide.with(context).asBitmap()
                .load(mediaController.metadata.description.iconUri)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) = callback.onBitmap(resource)

                    override fun onLoadCleared(placeholder: Drawable?) = Unit
                })
            return null
        }

    }
}