package com.dan.bassblock.feature_play_music.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.view.View
import androidx.activity.viewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.RequestManager
import com.dan.bassblock.R
import com.dan.bassblock.databinding.ActivityMainBinding
import com.dan.bassblock.feature_play_music.adapters.SwipeSongAdapter
import com.dan.bassblock.feature_play_music.domain.data.model.Song
import com.dan.bassblock.feature_play_music.domain.util.Status
import com.dan.bassblock.feature_play_music.exoplayer.extension.isPlaying
import com.dan.bassblock.feature_play_music.exoplayer.extension.toSong
import com.dan.bassblock.feature_play_music.viewmodel.MainViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    @Inject
    lateinit var swipeSongAdapter: SwipeSongAdapter

    @Inject
    lateinit var glide: RequestManager

    private lateinit var binding: ActivityMainBinding

    private var currentPlayingSong: Song? = null

    private var playbackState: PlaybackStateCompat? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView()
        this.initUI()
        this.subscribe()
    }

    private fun setContentView() {
        this.binding = ActivityMainBinding.inflate(this.layoutInflater)
        this.setContentView(binding.root)
    }

    private fun initUI() {
        this.binding.viewPagerSong.adapter = this.swipeSongAdapter
        this.setListeners()
        this.setupNavigation()
    }

    private fun setListeners() {
        this.binding.imageViewPausePlay.setOnClickListener {
            this.currentPlayingSong?.let {
                this.viewModel.toggleSong(it, true)
            }
        }

        this.binding.viewPagerSong.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val song = swipeSongAdapter.songs[position]
                if (playbackState?.isPlaying == true) {
                    viewModel.toggleSong(song)
                } else {
                    currentPlayingSong = song
                }
            }
        })
    }

    private fun setupNavigation() {
        val navHostFragment = this.supportFragmentManager
            .findFragmentById(R.id.fragment_container) as NavHostFragment

        navHostFragment.navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.songFragment -> this.hideCurrentSong()
                else -> showCurrentSong()
            }
        }

        this.swipeSongAdapter.setClickListener {
            navHostFragment.navController.navigate(R.id.songFragment)
        }
    }

    private fun switchToCurrentSong(song: Song) {
        val index = this.swipeSongAdapter.songs.indexOf(song)
        if (index != -1) {
            this.binding.viewPagerSong.currentItem = index
            this.currentPlayingSong = song
        }
    }

    private fun subscribe() {
        this.viewModel.mediaItems.observe(this) {
            it?.let { result ->
                if (result.status == Status.SUCCESS) {
                    result.data?.let { songs ->
                        this.swipeSongAdapter.songs = songs
                        if (songs.isNotEmpty()) {
                            this.glide
                                .load((currentPlayingSong ?: songs[0]).imageUrl)
                                .centerCrop()
                                .into(binding.imageViewSong)
                        }
                        this.switchToCurrentSong(currentPlayingSong ?: return@observe)
                    }
                }
            }
        }

        this.viewModel.currentPlayingSong.observe(this) {
            if (it == null) {
                return@observe
            }

            this.currentPlayingSong = it.toSong()
            this.glide
                .load(currentPlayingSong?.imageUrl)
                .centerCrop()
                .into(binding.imageViewSong)
            this.switchToCurrentSong(currentPlayingSong ?: return@observe)
        }

        this.viewModel.playbackState.observe(this) {
            this.playbackState = it
            this.binding.imageViewPausePlay.setImageResource(
                if (playbackState?.isPlaying == true) R.drawable.ic_pause else R.drawable.ic_play
            )
        }

        this.viewModel.connected.observe(this) {
            it?.getDataIfNotHandled()?.let { result ->
                if (result.status == Status.ERROR) {
                    Snackbar.make(
                        binding.root,
                        "message: " + result.message,
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
        }

        this.viewModel.networkError.observe(this) {
            it?.getDataIfNotHandled()?.let { result ->
                if (result.status == Status.ERROR) {
                    Snackbar.make(
                        binding.root,
                        "message: " + result.message,
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun hideCurrentSong() {
        this.binding.layoutCurrentSong.visibility = View.GONE
    }

    private fun showCurrentSong() {
        this.binding.layoutCurrentSong.visibility = View.VISIBLE
    }
}