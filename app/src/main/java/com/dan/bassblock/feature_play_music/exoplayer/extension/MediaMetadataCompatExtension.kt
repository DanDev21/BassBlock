package com.dan.bassblock.feature_play_music.exoplayer.extension

import android.support.v4.media.MediaMetadataCompat
import com.dan.bassblock.feature_play_music.domain.data.model.Song

fun MediaMetadataCompat.toSong(): Song? {
    return this.description?.let {
        Song(
            it.mediaId ?: "",
            it.title.toString(),
            it.subtitle.toString(),
            it.mediaUri.toString(),
            it.iconUri.toString()
        )
    }
}