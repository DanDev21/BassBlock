package com.dan.bassblock.feature_play_music.domain.util

object Constants {

    // firebase cloud database (fire_store)
    const val COLLECTION_SONGS = "songs"

    // music notification channel
    const val NOTIFICATION_CHANNEL_ID = "music"
    const val NOTIFICATION_ID = 1

    const val MEDIA_ROOT_ID = "media_root_id"

    // errors
    const val ERROR_NETWORK = "network_error"

    // intervals
    const val INTERVAL_PLAYER_POSITION_UPDATE = 100L

    // date formats
    const val DATE_FORMAT_MIN_SEC = "mm:ss"
}