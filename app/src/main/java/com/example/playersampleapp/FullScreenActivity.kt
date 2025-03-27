package com.example.playersampleapp

import android.os.Bundle
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.dash.DashMediaSource
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.PlayerView


class FullScreenActivity : AppCompatActivity() {
    private var player: ExoPlayer? = null
    private var playerView: PlayerView? = null

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_screen)

        playerView = findViewById(R.id.playerView)
        player = ExoPlayer.Builder(this).build()

        val videoUrl = intent.getStringExtra("VIDEO_URL") ?: ""
        val mediaSource: MediaSource = when {
            videoUrl.endsWith(".mp4") -> {
                val mediaItem: MediaItem = MediaItem.fromUri(videoUrl)
                val dataSourceFactory = DefaultHttpDataSource.Factory()
                ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(mediaItem)
            }
            videoUrl.endsWith(".m3u8") -> {
                // HLS format
                val mediaItem: MediaItem = MediaItem.fromUri(videoUrl)
                val dataSourceFactory = DefaultHttpDataSource.Factory()
                HlsMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(mediaItem)
            }
            videoUrl.endsWith(".mpd") -> {
                // DASH format
                val mediaItem: MediaItem = MediaItem.fromUri(videoUrl)
                val dataSourceFactory = DefaultHttpDataSource.Factory()
                DashMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(mediaItem)
            }
            else -> {
                throw IllegalArgumentException("Unsupported video format")
            }
        }

        player?.apply {
            setMediaSource(mediaSource)
            prepare()
            play()
        }

        playerView?.setPlayer(player)
    }

    override fun onStop() {
        super.onStop()
        player!!.release() // Oslobađanje resursa
    }
}