package com.example.playersampleapp.viewModel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.playersampleapp.activity.FullScreenActivity
import com.example.playersampleapp.extension.durationToTime
import com.example.playersampleapp.server.ACTrackSource
import com.example.playersampleapp.server.PlayMetadata
import com.example.playersampleapp.server.TrackSourceType
import com.example.playersampleapp.server.model.DeviceStatusDTO
import com.example.playersampleapp.server.model.PlaylistItemDTO
import com.example.playersampleapp.server.model.StatusType
import kotlin.math.roundToInt

class PlayerViewModel(application: Application, val player: ExoPlayer) : AndroidViewModel(application) {
    var playbackPosition: Long = 0
    private var currentMetadata: PlayMetadata? = null
    private var mContext: Context? = null

    fun load(playlist: List<PlaylistItemDTO>) {
        buildMetadata(playlist).let { tracks ->
            prepareMetadata(tracks)



            mContext?.let {
                val intent = Intent(mContext, FullScreenActivity::class.java)
                val urls = tracks.tracks.map { it.url }
                intent.putStringArrayListExtra("VIDEO_URLS", ArrayList(urls))
                it.startActivity(intent)

                play(tracks.tracks.first().mediaId)
            }
        }
    }

    fun pause() {
        player.playWhenReady = false
        player.pause()
    }

    fun play(mediaId: String) {
        Log.d("LAZA", "[acc] play metoda VM")
        currentMetadata?.tracks?.indexOfFirst { item -> mediaId == item.mediaId }?.takeIf { it != -1 }?.let {
            Log.d("LAZA", "[acc] play metoda VM nasao ga je")
            val index = player.currentMediaItemIndex
            if (index != it) player.seekTo(it, C.TIME_UNSET)
            player.playWhenReady = true
        }
        Log.d("LAZA", "[acc] play metoda VM zavrsio")
    }

    fun previous() {
        player.seekToPrevious()
    }

    fun next() {
        if (player.hasNextMediaItem()) {
            player.seekToNext()  // Prelazak na sledeći media item
            player.playWhenReady = true  // Pokreće reprodukciju odmah
        } else {
            Log.d("LAZA", "[ACC] No next media item")
        }
    }

    fun seekTo(position: Long) {
        player.seekTo(position)
    }

    fun stop() {
        player.stop()
        player.release()
    }

    fun mute(mute: Boolean) {
        player.volume = if (mute) 0f else 1f
    }

    @RequiresApi(Build.VERSION_CODES.P)
    fun setVolume(volume: Int) {
        val audioManager = mContext?.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val isVolumeControlSupported = audioManager.isVolumeFixed.not()
        val canAdjustMusicVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) != AudioManager.ADJUST_MUTE

        if (!isVolumeControlSupported || !canAdjustMusicVolume) {
            throw RuntimeException("Volume adjustment not supported")
        }

        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val minVolume = audioManager.getStreamMinVolume(AudioManager.STREAM_MUSIC)
        val targetVolume = minVolume + (volume * (maxVolume - minVolume) / 100f).roundToInt()

        player.volume = targetVolume.toFloat() / maxVolume
    }

    fun status(): DeviceStatusDTO {
        val index = player.currentMediaItemIndex
        val item = currentMetadata?.tracks?.getOrNull(index)
        return if (item != null) {
            val isPlaying = player.isPlaying
            val isBuffering = isBuffering()
            val isPaused = isPaused()
            val position = player.currentPosition / 1000.0f
            val status = when {
                isPlaying -> StatusType.PLAYING
                isBuffering -> StatusType.BUFFERING
                isPaused -> StatusType.PAUSED
                else -> StatusType.ENDED
            }
            DeviceStatusDTO(item.mediaId, status, position)
        } else {
            DeviceStatusDTO("1", StatusType.ENDED, 0.0f)
        }
    }

    private fun isBuffering() : Boolean {
        return player.playbackState == Player.STATE_BUFFERING
    }

    private fun isPaused() = player.run {
        playbackState == Player.STATE_READY && !playWhenReady
    }

    private fun buildMetadata(list: List<PlaylistItemDTO>) : PlayMetadata {
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

    private fun prepareMetadata(metadata: PlayMetadata?) {
        currentMetadata = metadata
    }

    fun setupWith(context: Context) {
        mContext = context
    }


}