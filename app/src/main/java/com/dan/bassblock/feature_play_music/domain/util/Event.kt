package com.dan.bassblock.feature_play_music.domain.util

open class Event<out T>(private val data: T) {

    var handled = false
        private set

    fun getDataIfNotHandled() =
        if (!handled) {
            handled = true
            data
        } else null
}