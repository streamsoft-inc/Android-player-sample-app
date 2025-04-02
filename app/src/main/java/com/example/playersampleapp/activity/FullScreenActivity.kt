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
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.dash.DashMediaSource
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.ConcatenatingMediaSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.PlayerView
import com.example.playersampleapp.R
import com.example.playersampleapp.viewModel.PlayerViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel


class FullScreenActivity : AppCompatActivity() {
    companion object {
        val ACTION_CLOSE_ACTIVITY: String = "com.yourapp.ACTION_CLOSE_ACTIVITY"
    }

    private lateinit var playerView: PlayerView
    private val playerViewModel: PlayerViewModel by viewModel()

    @RequiresApi(Build.VERSION_CODES.R)
    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_screen)

        //todo observer

        hideStatusBar()

        playerView = findViewById(R.id.playerView)

        val videoUrl = intent.getStringExtra("VIDEO_URL") ?: ""
        val videoUrls = intent.getStringArrayListExtra("VIDEO_URLS") ?: arrayListOf()

        playerViewModel.player.stop()
        playerViewModel.player.clearMediaItems()

        when {
            videoUrls.isNotEmpty() -> {
                val mediaSource = createMediaSources(videoUrls)
                playerViewModel.player.setMediaSource(mediaSource)
                playerViewModel.player.prepare()
            }
            videoUrl.isNotBlank() -> {
                val mediaSource = createMediaSource(videoUrl)
                playerViewModel.player.setMediaSource(mediaSource)
                playerViewModel.player.prepare()
            }
            else -> {
                Toast.makeText(this, "No videos to play", Toast.LENGTH_SHORT).show()
            }
        }

        playerView.player = playerViewModel.player
        playerViewModel.player.seekTo(playerViewModel.playbackPosition)
        playerViewModel.player.playWhenReady = true
    }

    @OptIn(UnstableApi::class)
    private fun createMediaSource(url: String): MediaSource {
        val mediaItem = MediaItem.fromUri(url)
        val dataSourceFactory = DefaultHttpDataSource.Factory()

        return when {
            url.endsWith(".mp4") -> ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)
            url.endsWith(".m3u8") -> HlsMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)
            url.endsWith(".mpd") -> DashMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)
            else -> throw IllegalArgumentException("Unsupported video format")
        }
    }

    @OptIn(UnstableApi::class)
    private fun createMediaSources(urls: List<String>): MediaSource {
        val dataSourceFactory = DefaultHttpDataSource.Factory()

        val mediaSources = urls.map { url ->
            val mediaItem = MediaItem.fromUri(url)

            when {
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

        // Create ConcatenatingMediaSource using the new API (media3)
        return ConcatenatingMediaSource(*mediaSources.toTypedArray())
    }

    override fun onStop() {
        super.onStop()
        playerViewModel.playbackPosition = playerViewModel.player.currentPosition
        playerViewModel.player.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
    }

    private fun releasePlayer() {
        playerViewModel.player.release()
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