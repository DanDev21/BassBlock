package com.dan.bassblock.feature_play_music.ui.fragment

import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.RequestManager
import com.dan.bassblock.R
import com.dan.bassblock.databinding.FragmentSongBinding
import com.dan.bassblock.feature_play_music.domain.data.model.Song
import com.dan.bassblock.feature_play_music.domain.util.Constants
import com.dan.bassblock.feature_play_music.domain.util.Status
import com.dan.bassblock.feature_play_music.exoplayer.extension.isPlaying
import com.dan.bassblock.feature_play_music.exoplayer.extension.toSong
import com.dan.bassblock.feature_play_music.viewmodel.MainViewModel
import com.dan.bassblock.feature_play_music.viewmodel.SongViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class SongFragment : Fragment(R.layout.fragment_song) {

    @Inject
    lateinit var glide: RequestManager

    private lateinit var binding: FragmentSongBinding

    private lateinit var mainViewModel: MainViewModel
    private val songViewModel: SongViewModel by viewModels()

    private var currentPlayingSong: Song? = null
    private var playbackState: PlaybackStateCompat? = null
    private var updateSeekBar = true

    private val dateFormatter =
        SimpleDateFormat(Constants.DATE_FORMAT_MIN_SEC, Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        this.binding = FragmentSongBinding.inflate(inflater, container, false)
        return this.binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.mainViewModel = ViewModelProvider(this.requireActivity())[MainViewModel::class.java]
        this.subscribe()
        this.initUI()
    }

    private fun subscribe() {
        this.mainViewModel.mediaItems.observe(viewLifecycleOwner) {
            it?.let { result ->
                if (result.status == Status.SUCCESS) {
                    result.data?.let { songs ->
                        if (this.currentPlayingSong == null && songs.isNotEmpty()) {
                            this.currentPlayingSong = songs[0]
                            this.updateSongImageAndTitle(songs[0])
                        }
                    }
                }
            }
        }

        this.mainViewModel.currentPlayingSong.observe(viewLifecycleOwner) {
            it?.let {
                it.toSong()?.let { song ->
                    this.currentPlayingSong = song
                    this.updateSongImageAndTitle(song)
                }
            }
        }

        this.mainViewModel.playbackState.observe(viewLifecycleOwner) {
            this.playbackState = it
            this.binding.imageViewPlayPause.setImageResource(
                if (this.playbackState?.isPlaying == true)  R.drawable.ic_pause
                else                                        R.drawable.ic_play
            )
            this.binding.seekBar.progress = it?.position?.toInt() ?: 0
        }

        this.songViewModel.currentPlayerPosition.observe(viewLifecycleOwner) {
            if (updateSeekBar) {
                this.binding.seekBar.progress = it.toInt()
                this.binding.textViewElapsed.text = this.dateFormatter.format(it)
            }
        }

        this.songViewModel.currentSongDuration.observe(viewLifecycleOwner) {
            this.binding.seekBar.max = it.toInt()
            this.binding.textViewDuration.text = this.dateFormatter.format(it)
        }
    }

    private fun updateSongImageAndTitle(song: Song) {
        val text = "${song.title} - ${song.artist}"
        this.binding.textViewTitle.text = text
        this.glide
            .load(song.imageUrl)
            .centerCrop()
            .into(binding.imageViewSong)
    }

    private fun initUI() {
        this.setListeners()
    }

    private fun setListeners() {
        this.binding.imageViewPlayPause.setOnClickListener {
            this.currentPlayingSong?.let {
                this.mainViewModel.toggleSong(it, true)
            }
        }

        this.binding.imageViewSkipNext.setOnClickListener {
            this.mainViewModel.skipToNext()
        }

        this.binding.imageViewSkipPrevious.setOnClickListener {
            this.mainViewModel.skipToPrevious()
        }

        this.binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    binding.textViewElapsed.text = dateFormatter.format(progress.toLong())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                updateSeekBar = false
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.let {
                    mainViewModel.seekTo(it.progress.toLong())
                    updateSeekBar = true
                }
            }
        })
    }
}