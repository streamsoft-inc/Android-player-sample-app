package com.example.playersampleapp.activity

import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.dash.DashMediaSource
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.PlayerView
import com.example.playersampleapp.viewModel.PlayerViewModel
import com.example.playersampleapp.R


class FullScreenActivity : AppCompatActivity() {
    private lateinit var playerViewModel: PlayerViewModel
    private lateinit var playerView: PlayerView


    @RequiresApi(Build.VERSION_CODES.R)
    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_screen)

        hideStatusBar()

        playerViewModel = ViewModelProvider(this)[PlayerViewModel::class.java]
        playerView = findViewById(R.id.playerView)

        if (playerViewModel.player == null) {
            playerViewModel.player = ExoPlayer.Builder(this).build()

            val videoUrl = intent.getStringExtra("VIDEO_URL") ?: ""
            val mediaSource: MediaSource = when {
                videoUrl.endsWith(".mp4") -> {
                    val mediaItem: MediaItem = MediaItem.fromUri(videoUrl)
                    val dataSourceFactory = DefaultHttpDataSource.Factory()
                    ProgressiveMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(mediaItem)
                }
                videoUrl.endsWith(".m3u8") -> {
                    val mediaItem: MediaItem = MediaItem.fromUri(videoUrl)
                    val dataSourceFactory = DefaultHttpDataSource.Factory()
                    HlsMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(mediaItem)
                }
                videoUrl.endsWith(".mpd") -> {
                    val mediaItem: MediaItem = MediaItem.fromUri(videoUrl)
                    val dataSourceFactory = DefaultHttpDataSource.Factory()
                    DashMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(mediaItem)
                }
                else -> {
                    throw IllegalArgumentException("Unsupported video format")
                }
            }

            playerViewModel.player?.apply {
                setMediaSource(mediaSource)
                prepare()
            }
        }

        playerView.player = playerViewModel.player
        playerViewModel.player?.seekTo(playerViewModel.playbackPosition)
        playerViewModel.player?.playWhenReady = true
    }

    override fun onStop() {
        super.onStop()
        playerViewModel.playbackPosition = playerViewModel.player?.currentPosition ?: 0
        playerViewModel.player?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
    }

    private fun releasePlayer() {
        playerViewModel.player?.release()
        playerViewModel.player = null
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun hideStatusBar() {
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            window.insetsController?.apply {
                hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
    }
}