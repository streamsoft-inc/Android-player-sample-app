package com.example.playersampleapp.server

import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import com.example.playersampleapp.extension.durationToTime
import com.example.playersampleapp.server.model.DeviceStatusDTO
import com.example.playersampleapp.server.model.PlaylistItemDTO
import com.example.playersampleapp.server.model.StatusType

interface AppMediaPlayer {
    fun load(playlist: List<PlaylistItemDTO>)

    fun play(mediaId: String)

    fun seek(positionMs: Long)

    fun stop()

    fun playlist() : PlaylistItemDTO?

    fun currentTrack() : ACTrackSource

    fun next()
    fun previous()
    fun setVolume(volume: Int)
    fun mute(mute: Boolean)
    fun pause()

    fun status() : DeviceStatusDTO?

    fun isPlayingInBg() : Boolean

    fun setupWith(context: Context)
}

fun buildMetadata(list: List<PlaylistItemDTO>) : PlayMetadata {
    val tracks = list.map {
        val ismhm = it.metadata.format == "SONY"

        ACTrackSource(
            -1,
            it.id,
            it.id,
            TrackSourceType.External,
            it.url,
            -1,null,it.metadata.title,
            "",it.duration.durationToTime().toDouble(), it.type.contentEquals("VIDEO", ignoreCase = true),
            legacyUrl = false, isRAtype = it.metadata.format == "SONY",
            isAuroType = it.metadata.format == "AURO", isMHM1Format = ismhm, isMHA1Format = !ismhm,
            false, false,null, false, false,
            artworkUrl = if( it.metadata.artworkUrl != null) Uri.parse(it.metadata.artworkUrl) else Uri.EMPTY
        )
    }

    return PlayMetadata(
        Uri.EMPTY,
        "",
        "",
        -1,
        null,
        null,
        "",
        -1,
        tracks = tracks
    )
}

class MobileAppMediaPlayer(val acPlayer: ACPlayer) : AppMediaPlayer{
    private var lastStatus: DeviceStatusDTO? = null
    private var metadata: PlayMetadata? = null

    override fun load(playlist: List<PlaylistItemDTO>) {
        metadata = buildMetadata(playlist)
        acPlayer.play(metadata!!, true, 0)
    }
    override fun setupWith(context: Context) {
        // NOOP
    }

    override fun play(mediaId: String) {
        if (metadata == null) return
        metadata?.tracks?.indexOfFirst { item -> mediaId == item.mediaId }?.takeIf { it != -1 }?.let {
            val index = acPlayer.exoPlayer.currentMediaItemIndex
            if (index != it) acPlayer.moveTo(it)
            acPlayer.exoPlayer.playWhenReady = true
        }
//        readStatus()
    }

    override fun pause() {
        if (metadata == null) return
        acPlayer.exoPlayer.pause()
    }

    override fun seek(positionMs: Long) {
        if (metadata == null) return
        try {
            acPlayer.exoPlayer.seekTo(positionMs)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun stop() {
        acPlayer.stop()
        metadata = null
    }

    override fun playlist(): PlaylistItemDTO? {
        return null
    }

    override fun currentTrack(): ACTrackSource {
        TODO("Not yet implemented")
    }

    override fun next() {
        if (metadata == null) return
        acPlayer.exoPlayer.seekToNext()
    }

    override fun previous() {
        if (metadata == null) return
        acPlayer.exoPlayer.seekToPrevious()
    }

    override fun setVolume(volume: Int) {
        acPlayer.setVolume(volume)
    }

    override fun mute(mute: Boolean) {
        acPlayer.mute(mute)
    }

    override fun status(): DeviceStatusDTO? {
        readStatus()
        return lastStatus
    }


    @OptIn(UnstableApi::class)
    private fun readStatus() {
        val index = acPlayer.exoPlayer.currentWindowIndex
        val item = metadata?.tracks?.getOrNull(index)
        if (item != null) {
            val isPlaying = acPlayer.exoPlayer.isPlaying
            val isBuffering = acPlayer.exoPlayer.playbackState == Player.STATE_BUFFERING
            val isPaused = acPlayer.exoPlayer.run {
                playbackState == Player.STATE_READY && !playWhenReady
            }
            val position =  acPlayer.exoPlayer.currentPosition / 1000.0f
            val status = when{
                isPlaying -> StatusType.PLAYING
                isBuffering -> StatusType.BUFFERING
                isPaused -> StatusType.PAUSED
                else -> StatusType.ENDED
            }
            lastStatus = DeviceStatusDTO(item.mediaId, status, position)
        } else {
            lastStatus = DeviceStatusDTO("1", StatusType.ENDED, 0.0f)
        }
    }

    override fun isPlayingInBg(): Boolean {
        return false
    }
}