package com.example.playersampleapp.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.media3.exoplayer.ExoPlayer

class PlayerViewModel(application: Application) : AndroidViewModel(application) {
    var player: ExoPlayer? = null
    var playbackPosition: Long = 0
}