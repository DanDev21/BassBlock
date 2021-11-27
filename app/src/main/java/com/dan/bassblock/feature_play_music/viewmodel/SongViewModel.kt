package com.dan.bassblock.feature_play_music.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dan.bassblock.feature_play_music.domain.util.Constants
import com.dan.bassblock.feature_play_music.exoplayer.MusicServiceConnection
import com.dan.bassblock.feature_play_music.exoplayer.extension.currentPlaybackPosition
import com.dan.bassblock.feature_play_music.service.exoplayer.MusicService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SongViewModel @Inject constructor(
    musicServiceConnection: MusicServiceConnection
) : ViewModel() {

    private val playbackState = musicServiceConnection.playbackState

    private val _currentSongDuration = MutableLiveData<Long>()
    val currentSongDuration: LiveData<Long> = this._currentSongDuration

    private val _currentPlayerPosition = MutableLiveData<Long>()
    val currentPlayerPosition: LiveData<Long> = this._currentPlayerPosition

    init {
        this.updateCurrentPlayerPosition()
    }

    private fun updateCurrentPlayerPosition() = viewModelScope.launch {
        while (true) {
            val position = playbackState.value
                ?.currentPlaybackPosition
                ?: return@launch

            if (currentPlayerPosition.value != position) {
                _currentPlayerPosition.postValue(position)
                _currentSongDuration.postValue(MusicService.currentSongDuration)
            }
            delay(Constants.INTERVAL_PLAYER_POSITION_UPDATE)
        }
    }
}