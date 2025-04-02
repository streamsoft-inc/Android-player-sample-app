package com.example.playersampleapp.server

import android.content.Context
import android.media.AudioManager
import android.media.MediaMetadata
import android.os.Build
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.Cache
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.dash.DashMediaSource
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.ConcatenatingMediaSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.example.playersampleapp.server.model.PlayerTrackEvent
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import kotlin.math.roundToInt

class TVAudioPlayer
@OptIn(UnstableApi::class) constructor
    (private val context: Context, val cache: Cache) {

    private lateinit var mediaSource: MediaSource
    val exoPlayer = ExoPlayer.Builder(context).build()
    private val metadataSubject = BehaviorSubject.create<PlayMetadata>()

    var seekIndex: Int = 0
//    var playbackService: TVPlaybackMediaService? = null

//    var serviceConnection = object : ServiceConnection {
//        @OptIn(UnstableApi::class)
//        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
//            exoPlayer.setMediaSource(mediaSource)
//            exoPlayer.prepare()
//            exoPlayer.play()
//            seekTo(seekIndex, C.TIME_UNSET)
//
//            playbackService = (service as LocalBinder).service
//            playbackService!!.mediaSession.setCallback(object : MediaSessionCompat.Callback() {
//                override fun onSeekTo(pos: Long) {
//                    seekTo(pos)
//                }
//
//                override fun onPlay() {
//                    play()
//                }
//
//                override fun onPause() {
//                    pause()
//                }
//
//                override fun onSkipToNext() {
//                    exoPlayer.seekToNext()
//                }
//
//                override fun onSkipToPrevious() {
//                    exoPlayer.seekToPrevious()
//                }
//
//                override fun onStop() {
//                    stop(true)
//                }
//            })
//        }
//
//        override fun onServiceDisconnected(name: ComponentName?) {
//            playbackService = null
//        }
//    }

    var currentMetadata: PlayMetadata? = null

    fun prepareMetadata(metadata: PlayMetadata?) {
        currentMetadata = metadata
        if (metadata != null)
            metadataSubject.onNext(metadata)
    }

    private val currentTrackSubject = BehaviorSubject.create<PlayerTrackEvent>()

    init {
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    val currentTrack = currentMetadata?.tracks?.getOrNull(exoPlayer.currentWindowIndex)
                    if (currentTrack != null) {
                        attachMediaInfo(currentTrack.trackName, currentTrack.playlistName)
                    }
                }
            }
        })
    }

    private fun attachMediaInfo(trackName: String, playlistName: String) {
        val mediaBuilder = MediaMetadataCompat.Builder()
            .putString(MediaMetadata.METADATA_KEY_TITLE, playlistName)
            .putString(MediaMetadata.METADATA_KEY_ARTIST, trackName)

        mediaBuilder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, exoPlayer.duration)

        val actions = PlaybackStateCompat.ACTION_PLAY_PAUSE or
                PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PAUSE or
                PlaybackStateCompat.ACTION_SEEK_TO

//        val mediaSession = playbackService?.mediaSession ?: return
//
//        mediaSession.setPlaybackState(
//            PlaybackStateCompat.Builder()
//                .setActions(actions)
//                .setState(
//                    if (exoPlayer.playWhenReady) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED,
//                    exoPlayer.currentPosition.takeIf { it > 0 } ?: 1000, 1f
//                ).build()
//        )
//
//        mediaSession.setMetadata(mediaBuilder.build())
    }

    fun currentTrack(): Observable<PlayerTrackEvent> {
        return currentTrackSubject
    }

    fun currentTrackPlaying(): ACTrackSource? {
        return currentTrackSubject.value?.track
    }

    fun isIdle() = exoPlayer.playbackState == Player.STATE_IDLE

    fun getCurrentPlaying(): Observable<PlayMetadata> {
        return metadataSubject
    }

//    fun prepare(audioList: List<ACTrackSource>, index: Int, shouldPlay: Boolean) {
//        mediaSource = createMediaSources(audioList)
//        this.seekIndex = index
//
//        if (playbackService != null) {
//            exoPlayer.setMediaSource(mediaSource)
//            exoPlayer.prepare()
//            if (shouldPlay) {
//                exoPlayer.play()
//                seekTo(index, C.TIME_UNSET)
//            }
//        } else startMusicService()
//    }

//    private fun startMusicService() {
//        val intent = Intent(context, TVPlaybackMediaService::class.java)
//        context.startService(intent)
//        context.bindService(intent, serviceConnection, 0)
//    }
//
     fun stop(reset: Boolean) {
//        val intent = Intent(context, TVPlaybackMediaService::class.java)
//        context.stopService(intent)

        exoPlayer.stop()
        exoPlayer.release()
    }

//    private fun isFirstSong() = exoPlayer.currentWindowIndex == 0
//
//    private fun isLastSong() = exoPlayer.currentWindowIndex == (this.currentMetadata?.tracks?.size?.minus(1) ?: -1)

    fun mute(mute: Boolean) {
        exoPlayer.volume = if (mute) 0f else 1f
    }

    @RequiresApi(Build.VERSION_CODES.P)
    fun setVolume(volume: Int) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val isVolumeControlSupported = audioManager.isVolumeFixed.not()
        val canAdjustMusicVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) != AudioManager.ADJUST_MUTE

        if (!isVolumeControlSupported || !canAdjustMusicVolume) {
            throw RuntimeException("Volume adjustment not supported")
        }

        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val minVolume = audioManager.getStreamMinVolume(AudioManager.STREAM_MUSIC)
        val targetVolume = minVolume + (volume * (maxVolume - minVolume) / 100f).roundToInt()

        exoPlayer.volume = targetVolume.toFloat() / maxVolume
    }

    fun play(mediaId: String) {
        currentMetadata?.tracks?.indexOfFirst { item -> mediaId == item.mediaId }?.takeIf { it != -1 }?.let {
            val index = exoPlayer.currentMediaItemIndex
            if (index != it) exoPlayer.seekTo(it, C.TIME_UNSET)
            exoPlayer.playWhenReady = true
        }
    }

    fun seekTo(position:Long) {
        exoPlayer.seekTo(position)
    }

    fun next() {
        exoPlayer.seekToNext()
    }

    fun previous() {
        exoPlayer.seekToPrevious()
    }

    fun pause() {
        exoPlayer.pause()
    }

    fun currentWindowIndex() : Int {
        return exoPlayer.currentMediaItemIndex
    }

    fun currentPosition() : Long {
        return exoPlayer.currentPosition
    }


    fun isPlaying() : Boolean {
        return exoPlayer.isPlaying
    }

//    private fun createMediaSources(sources: List<ACTrackSource>): MediaSource {
//        val mediaSources: Array<MediaSource> = sources
//            .map {
//                val source = when (it.sourceType) {
//                    TrackSourceType.Cloud -> {
//                        val cloudFactory = DefaultHttpDataSourceFactory("AC")
//                        createSingleMediaSource(Uri.parse(it.url), CacheDataSourceFactory(cache, cloudFactory))
//                    }
//                    TrackSourceType.External -> {
//                        createSingleMediaSource(Uri.parse(it.url), CacheDataSourceFactory(cache, DefaultHttpDataSourceFactory("AC")))
//                    }
//                    else -> throw IllegalArgumentException("Source type not supported")
//                }
//                source
//            }.toTypedArray()
//
//        return ConcatenatingMediaSource(*mediaSources)
//    }

    @OptIn(UnstableApi::class)
    private fun createSingleMediaSource(url: String): MediaSource {
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

    fun isBuffering() : Boolean {
        return exoPlayer.playbackState == Player.STATE_BUFFERING
    }

    fun isPaused() = exoPlayer.run {
        playbackState == Player.STATE_READY && !playWhenReady
    }
}