package com.example.playersampleapp.server.model

import java.io.File

sealed class MediaCommands(var hasBody: Boolean = false) {
    class Load(var playlist: List<PlaylistItemDTO> = emptyList()): MediaCommands(true)
    class Play(var play: PlayDTO): MediaCommands(hasBody = true)
    object Pause: MediaCommands()
    object Next: MediaCommands()
    object Previous: MediaCommands()
    object Stop: MediaCommands()
    class Mute(var muteDTO: MuteDTO): MediaCommands()
    class SetVolume(var volumeDTO: VolumeDTO): MediaCommands()
    class SeekTo(var seekDTO: SeekDTO): MediaCommands(hasBody = true)
    class Connect(var connectDTO: ConnectDTO): MediaCommands()
    class Disconnect(): MediaCommands()
}


class PlayerConfig(
    val playerHost: String,
    val playerPort: String,
    val playerAPIConfig: PlayerAPIEndpoint = basePlayerAPIEndpoint
)

class PlayerAPIEndpoint(
    val load: String,
    val play: String,
    val pause: String,
    val next: String,
    val previous: String,
    val seek: String,
    val stop: String,
    val capabilities: String,
    val status: String,
    val volume: String,
    val mute: String,
    val connect: String,
    val disconnect: String,
)

val basePlayerAPIEndpoint = PlayerAPIEndpoint(
    load = "/media/load/playlist",
    play = "/media/play",
    pause = "/media/pause",
    next = "/media/next",
    previous = "/media/previous",
    seek = "/media/move",
    stop = "/media/stop",
    capabilities = "/device/capabilities",
    status = "/media/status",
    mute = "/media/mute",
    volume = "/media/volume",
    connect = "/device/connect",
    disconnect = "/device/disconnect"
)

@kotlinx.serialization.Serializable
class PlayDTO(val id: String)

@kotlinx.serialization.Serializable
class SeekDTO(val position: Float)

@kotlinx.serialization.Serializable
class MuteDTO(val value: Boolean)

@kotlinx.serialization.Serializable
class VolumeDTO(val value: Float)

@kotlinx.serialization.Serializable
class ConnectDTO(val name: String, val version: String)

@kotlinx.serialization.Serializable
class PlaylistItemDTO(
    val id: String,
    val url: String,
    val type: String,
    val duration: String,
    val metadata: MetadataDTO
)

@kotlinx.serialization.Serializable
class MetadataDTO(
    val title: String,
    val artistName: String,
    val albumName: String,
    val artworkUrl: String? = null,
    val format: String
)

@kotlinx.serialization.Serializable
class DeviceStatusDTO(
    val id: String,
    val state: StatusType,
    val position: Float? = 0.0f
)

@kotlinx.serialization.Serializable
class ErrorResponceDTO(
    val error:String,
    val code: Int?
)


class PlayerStatusDTO(
    var status: PlayerInfoStatus,
    val deviceStatusDTO: DeviceStatusDTO?
)

enum class PlayerInfoStatus {
    CONNECTED, DISCONNECTED, MEDIA_LOADED, STOPPED, IDLE, ERROR
}

enum class StatusType {
    PLAYING, PAUSED, BUFFERING, ENDED
}

data class PlaybackCloudConfig(
    val playbackEndpoint: String,
    val downloadEndpoint: String
)

data class FileDownloaderConfig(
    val numOfParallelDownloads: Int = 1,
    val downloadDirectory: File
)