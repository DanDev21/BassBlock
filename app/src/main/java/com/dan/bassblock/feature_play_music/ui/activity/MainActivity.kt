package com.dan.bassblock.feature_play_music.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.dan.bassblock.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {


    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.binding = ActivityMainBinding.inflate(this.layoutInflater)
        this.setContentView(binding.root)
    }
}