package com.example.playersampleapp.server

import android.database.Observable
import android.net.Uri
import androidx.media3.common.Player


sealed class TrackSourceType {

    object Cloud : TrackSourceType()
    object External : TrackSourceType()
    object Local : TrackSourceType()
    object Unknown : TrackSourceType()
}

data class PlayMetadata(
    val imageUri: Uri = Uri.EMPTY,
    val title: String = "",
    val desc: String = "",
    val projectID: Long = -1,
    val referencedStudioId: Int? = null,
    val projectShareableChannelId: String? = null,
    val shareableId: String = "",
    val albumID: Long = -1,
    val tracks: List<ACTrackSource> = listOf()
)

data class ACTrackSource(
    val id: Long,
    val mediaId: String,
    val streamingMediaId:String,
    val sourceType: TrackSourceType,
    var url: String,
    val projectID: Long,
    val projectShareableChannelId: String?,
    var trackName: String,
    val playlistName: String,
    val duration: Double,
    val isVideo: Boolean = false,
    var legacyUrl: Boolean = true,
    var isRAtype: Boolean = false,
    var isAuroType: Boolean = false,
    var isMHM1Format: Boolean = false,
    var isMHA1Format: Boolean = false,
    var isLiveShow: Boolean = false,
    var isLiveShowType: Boolean = false,
    var liveDonateInfo: String? = null,
    val isMultiSource: Boolean = false,
    var isDownloaded: Boolean = false,
    val artworkUrl: Uri = Uri.EMPTY
)

sealed class PlayerType {
    object ExoPlayer : PlayerType()
    object CastPlayer : PlayerType()
    object CloudError : PlayerType()
}

interface ACPlayer {

    val exoPlayer: Player

    var castEnabled: Boolean

    fun playlist(): Observable<PlayMetadata>

    fun play(acPlaylist: PlayMetadata, useSeek: Boolean = false, playlistIndex: Int = -1)

    fun moveTo(playlistIndex: Int)

    fun moveTo(playlistIndex: Int, positionMs: Long)

    fun stop()

    val isIdle: Boolean

    val playWhenReady: Boolean

    fun isCastCurrent(): Boolean

    val canCastVideo: Boolean

    fun playerChanged(): Observable<PlayerType>

    fun mute(mute: Boolean)

    fun setVolume(volume: Int) // 0 - 100
}