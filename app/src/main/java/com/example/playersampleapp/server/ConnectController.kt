package com.example.playersampleapp.server

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.util.Util
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.cache.Cache
import androidx.media3.exoplayer.dash.DashMediaSource
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.smoothstreaming.SsMediaSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.extractor.DefaultExtractorsFactory
import com.example.playersampleapp.server.model.DeviceStatusDTO
import com.example.playersampleapp.server.model.MediaCommands
import com.example.playersampleapp.shared.MainThreadDispatcher

class ConnectController(
    val mediaHttpServer: EmbeddedMediaHttpServer,
    val appMediaPlayer: AppMediaPlayer,
    private val nsdController: NSDController,
    @SuppressLint("UnsafeOptInUsageError") private val cache: Cache,
) {
    private var mContext: Context? = null
    private var nsdStarted: Boolean = false
    private val status: ServiceStatusListener = {
        it?.let {
            println("status change - $it")
        }
    }

    private val mediaServerCallback : HttpCallbackResponse = {

        it?.payload?.onSuccess { command ->
            Log.d("LAZA", "COMMANDA $command")
            when (command) {
                is MediaCommands.Connect -> {

                    this.mContext?.let {
                        val toast = Toast.makeText(it, "Connected to " + command.connectDTO.name, Toast.LENGTH_LONG)
                        toast.show()
                    }
                }
                is MediaCommands.Disconnect -> {
                    this.mContext?.let {
                        val toast =
                            Toast.makeText(it, "Disconnected from device", Toast.LENGTH_LONG)
                        toast.show()
                    }
                }
                is AddressInfo -> {
                    println("[ACC] callback received ${command.host} - ${command.port}")

                    nsdController.status = status
                    nsdStarted = nsdController.start(command.port)
                }
                is MediaCommands.Load -> {
                    println("[ACC] load")
                    appMediaPlayer.load(command.playlist)
                }
                is MediaCommands.Pause -> {
                    println("[ACC] pause")
                    appMediaPlayer.pause()
                }
                is MediaCommands.Play -> {
                    println("[ACC] play")
                    readStatus()
                    appMediaPlayer.play(command.play.id)
                }
                is MediaCommands.Previous -> {
                    println("[ACC] previous")
                    appMediaPlayer.previous()
                }
                is MediaCommands.Next -> {
                    println("[ACC] next")
                    appMediaPlayer.next()
                }
                is MediaCommands.Stop -> {
                    println("[ACC] stop")
                    appMediaPlayer.stop()
                }
                is MediaCommands.SeekTo -> {
                    val position = command.seekDTO.position
                    println("[ACC] seek to $position")
                    appMediaPlayer.seek((position * 1000).toLong())
                }
                is MediaCommands.Mute -> {
                    val mute = command.muteDTO
                    println("[ACC] mute to $mute")
                    appMediaPlayer.mute(mute.value)
                }
                is MediaCommands.SetVolume -> {
                    val volume = command.volumeDTO.value
                    println("[ACC] volume to $volume")
                    appMediaPlayer.setVolume((volume * 100).toInt())
                }
            }
        }
    }

    private var lastStatus: DeviceStatusDTO? = null

    private fun readStatus() {
        lastStatus = appMediaPlayer.status()
    }
    fun start(context: Context) {
        Log.d("LAZA", "STARTUJ SERVER")
        mContext = context
        Thread{
            mediaHttpServer.stop()
            nsdController.stop()
            appMediaPlayer.setupWith(context)
            nsdController.setupWith(context)
            Thread.sleep(2000)
            mediaHttpServer.callback = mediaServerCallback
            mediaHttpServer.requestCallbackChannel = object : RequestCallbackChannel {
                override fun status(): DeviceStatusDTO? {
                    MainThreadDispatcher.post {
                        readStatus()
                    }
                    return lastStatus
                }
            }
            mediaHttpServer.start()

        }.start()

    }

    fun stop() {
        mediaHttpServer.stop()
        if (nsdStarted) nsdController.stop()
    }


    @OptIn(markerClass = [androidx.media3.common.util.UnstableApi::class])
    private fun createSingleMediaSource(
        uri: Uri,
        dataSourceFactory: DataSource.Factory
    ): MediaSource {
        val mediaItem = MediaItem.fromUri(uri)
        val type = Util.inferContentType(uri)
        return when (type) {
            C.TYPE_DASH -> DashMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)
            C.TYPE_SS -> SsMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)
            C.TYPE_HLS -> HlsMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)
            else -> ProgressiveMediaSource.Factory(dataSourceFactory, DefaultExtractorsFactory())
                .createMediaSource(mediaItem)
        }
    }
}