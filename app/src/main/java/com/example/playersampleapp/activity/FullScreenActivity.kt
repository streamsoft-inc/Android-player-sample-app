package com.example.playersampleapp.activity

import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.Toast
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
import com.example.playersampleapp.R
import com.example.playersampleapp.server.PlaybackFragmentArgs
import com.example.playersampleapp.viewModel.PlayerViewModel


class FullScreenActivity : AppCompatActivity() {
    companion object {
        val ACTION_CLOSE_ACTIVITY: String = "com.yourapp.ACTION_CLOSE_ACTIVITY"
    }

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

        val fragmentArgs = PlaybackFragmentArgs.fromBundle(intent.extras)
        Log.d("LAZA", "FRAGMENT ARGS ${fragmentArgs.index} ${fragmentArgs.preloaded} ${fragmentArgs.liveShow}  ${fragmentArgs.playlist}")

        if (playerViewModel.player == null) {
            playerViewModel.player = ExoPlayer.Builder(this).build()

            val videoUrl = intent.getStringExtra("VIDEO_URL") ?: ""
            videoUrl.takeIf { it.isNotBlank() }?.let { url ->
                val mediaSource = createMediaSource(url)

                playerViewModel.player?.apply {
                    setMediaSource(mediaSource)
                    prepare()
                }
            } ?: run {
                Toast.makeText(this, "URL is empty", Toast.LENGTH_SHORT).show()
            }
        }

        playerView.player = playerViewModel.player
        playerViewModel.player?.seekTo(playerViewModel.playbackPosition)
        playerViewModel.player?.playWhenReady = true
    }

    @OptIn(UnstableApi::class)
    private fun createMediaSource(url: String): MediaSource {
        val mediaItem = MediaItem.fromUri(url)
        val dataSourceFactory = DefaultHttpDataSource.Factory()

        return when {
            url.endsWith(".mp4") -> {
                ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)
            }
            url.endsWith(".m3u8") -> {
                HlsMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)
            }
            url.endsWith(".mpd") -> {
                DashMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)
            }
            else -> throw IllegalArgumentException("Unsupported video format")
        }
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