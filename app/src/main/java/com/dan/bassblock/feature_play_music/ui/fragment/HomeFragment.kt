package com.dan.bassblock.feature_play_music.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.dan.bassblock.R
import com.dan.bassblock.databinding.FragmentHomeBinding
import com.dan.bassblock.feature_play_music.adapters.SongAdapter
import com.dan.bassblock.feature_play_music.domain.data.model.Song
import com.dan.bassblock.feature_play_music.domain.util.Status
import com.dan.bassblock.feature_play_music.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var viewModel: MainViewModel
    private lateinit var binding: FragmentHomeBinding

    @Inject
    lateinit var songAdapter: SongAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        this.binding = FragmentHomeBinding.inflate(inflater, container, false)
        return this.binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.initUI()
        this.viewModel = ViewModelProvider(this.requireActivity())[MainViewModel::class.java]
        this.subscribeToObservers()

        this.songAdapter.setOnClickListener(this.viewModel::toggleSong)
    }

    private fun initUI() {
        this.setupRecyclerView()
    }

    private fun setupRecyclerView() = this.binding.recyclerViewSongs.apply {
        this.adapter = songAdapter
        this.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun subscribeToObservers() {
        this.viewModel.mediaItems.observe(viewLifecycleOwner) { result ->
            when (result.status) {
                Status.SUCCESS -> result.data?.let { this.displaySongs(it) }
                Status.LOADING -> this.binding.progressBar.visibility = View.VISIBLE
                Status.ERROR -> Unit
            }
        }
    }

    private fun displaySongs(songs: List<Song>) {
        this.binding.progressBar.visibility = View.GONE
        this.songAdapter.songs = songs
    }
}