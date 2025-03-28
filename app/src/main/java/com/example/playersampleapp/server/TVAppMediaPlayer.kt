package com.example.playersampleapp.server

import android.content.Context
import android.content.Intent
import com.example.playersampleapp.activity.FullScreenActivity
import com.example.playersampleapp.server.model.DeviceStatusDTO
import com.example.playersampleapp.server.model.PlaylistItemDTO
import com.example.playersampleapp.server.model.StatusType

class TVAppMediaPlayer(/*val tvAudioPlayer: TVAudioPlayer*/) : AppMediaPlayer{
    init {
//        println("instance_code in player: ${tvAudioPlayer.hashCode()}")
    }
    private var mContext: Context? = null
    override fun load(playlist: List<PlaylistItemDTO>) {
        buildMetadata(playlist).let { tracks ->
//            tvAudioPlayer.prepareMetadata(tracks)
            mContext?.let{
                Intent( mContext, FullScreenActivity::class.java ).also{ intent ->
                    intent.putExtras(PlaybackFragmentArgs(preloaded = true).toBundle())
                    it.startActivity(intent)
                }
            }
        }

    }
    override fun setupWith(context: Context) {
        mContext = context
    }

    override fun play(mediaId: String) {
//        tvAudioPlayer.play(mediaId)
    }

    override fun seek(positionMs: Long) {
//        tvAudioPlayer.seekTo(positionMs)
    }

    override fun stop() {
//        tvAudioPlayer.stop()
        mContext?.sendBroadcast(Intent(FullScreenActivity.ACTION_CLOSE_ACTIVITY))
    }

    override fun playlist(): PlaylistItemDTO? {
        TODO("Not yet implemented")
    }

    override fun currentTrack(): ACTrackSource {
        TODO("Not yet implemented")
    }

    override fun next() {
//        tvAudioPlayer.next()
    }

    override fun previous() {
//        tvAudioPlayer.previous()
    }

    override fun setVolume(volume: Int) {
//        tvAudioPlayer.setVolume(volume)
    }

    override fun mute(mute: Boolean) {
//        tvAudioPlayer.mute(mute)
    }

    override fun pause() {
//        tvAudioPlayer.pause()
    }

    override fun status(): DeviceStatusDTO? {
//        val index = tvAudioPlayer.currentWindowIndex
//        val item = tvAudioPlayer.currentMetadata?.tracks?.getOrNull(index)
//        return if (item != null) {
//            val isPlaying = tvAudioPlayer.isPlaying
//            val isBuffering = tvAudioPlayer.isBuffering()
//            val isPaused = tvAudioPlayer.isPaused()
//            val position = tvAudioPlayer.currentPosition / 1000.0f
//            val status = when {
//                isPlaying -> StatusType.PLAYING
//                isBuffering -> StatusType.BUFFERING
//                isPaused -> StatusType.PAUSED
//                else -> StatusType.ENDED
//            }
//            DeviceStatusDTO(item.mediaId, status, position)
//        } else {
//            DeviceStatusDTO("1", StatusType.ENDED, 0.0f)
//        }

        return DeviceStatusDTO("1", StatusType.ENDED, 0.0f)

    }

    override fun isPlayingInBg(): Boolean {
//        return TVPlaybackMediaService.isServiceRunning
        return false
    }
}