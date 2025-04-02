package com.example.playersampleapp.server

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.playersampleapp.activity.FullScreenActivity
import com.example.playersampleapp.server.model.DeviceStatusDTO
import com.example.playersampleapp.server.model.PlaylistItemDTO
import com.example.playersampleapp.server.model.StatusType
import com.example.playersampleapp.viewModel.PlayerViewModel

class TVAppMediaPlayer(private val tvAudioPlayer: TVAudioPlayer, private val viewModel: PlayerViewModel) : AppMediaPlayer{

    private var mContext: Context? = null

    override fun load(playlist: List<PlaylistItemDTO>) {

        buildMetadata(playlist).let { tracks ->
            tvAudioPlayer.prepareMetadata(tracks)
            mContext?.let{
//                Intent( mContext, FullScreenActivity::class.java ).also{ intent ->
//                    intent.putExtras(PlaybackFragmentArgs(preloaded = true).toBundle())
//                    it.startActivity(intent)
//                }


                val intent = Intent(mContext, FullScreenActivity::class.java)
                intent.putExtra("VIDEO_URL", tracks.tracks.first().url)
                it.startActivity(intent)

                tvAudioPlayer.play(tracks.tracks.first().mediaId)


            }
        }
    }
    override fun setupWith(context: Context) {
        mContext = context
    }

    override fun play(mediaId: String) {
        tvAudioPlayer.play(mediaId)
    }

    override fun seek(positionMs: Long) {
        tvAudioPlayer.seekTo(positionMs)
    }

    override fun stop() {
        tvAudioPlayer.stop(true)
        mContext?.sendBroadcast(Intent(FullScreenActivity.ACTION_CLOSE_ACTIVITY))
    }

    override fun playlist(): PlaylistItemDTO? {
        TODO("Not yet implemented")
    }

    override fun currentTrack(): ACTrackSource {
        TODO("Not yet implemented")
    }

    override fun next() {
        tvAudioPlayer.next()
    }

    override fun previous() {
        tvAudioPlayer.previous()
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun setVolume(volume: Int) {
        tvAudioPlayer.setVolume(volume)
    }

    override fun mute(mute: Boolean) {
        tvAudioPlayer.mute(mute)
    }

    override fun pause() {
        Log.d("LAZA", "STIGLA KOMANDA PAUSE")
//        tvAudioPlayer.pause()
        viewModel.pause()
    }

    override fun status(): DeviceStatusDTO {
        val index = tvAudioPlayer.currentWindowIndex()
        val item = tvAudioPlayer.currentMetadata?.tracks?.getOrNull(index)
        return if (item != null) {
            val isPlaying = tvAudioPlayer.isPlaying()
            val isBuffering = tvAudioPlayer.isBuffering()
            val isPaused = tvAudioPlayer.isPaused()
            val position = tvAudioPlayer.currentPosition() / 1000.0f
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

        return DeviceStatusDTO("1", StatusType.ENDED, 0.0f)

    }

    override fun isPlayingInBg(): Boolean {
//        return TVPlaybackMediaService.isServiceRunning
        return false
    }
}