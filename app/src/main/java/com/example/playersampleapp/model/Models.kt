package com.example.playersampleapp.model

import android.os.Parcelable
import androidx.room.Ignore
import androidx.room.TypeConverters
import com.example.playersampleapp.shared.AlbumTracksTypeConverter
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import java.io.Serializable

class Models {

    @Parcelize
    data class Album(
        val id: Long = -1,
        val name: String = "",
        @SerializedName("imageUrl")
        val imageURL: String = "",
        @TypeConverters(AlbumTracksTypeConverter::class)
        @SerializedName("mediaFiles")
        val tracks: List<MediaFileInfo> = listOf(),
        @SerializedName("artist")
        val artist: Artist?,
        val description: String? = null,
        @Ignore
        val projectID: Long = -1,
        @Ignore
        val projectShareableChannelId: String? = null,
        @Ignore
        val projectShareableId: String = "",
        @Ignore
        val referencedStudioId: Int? = null,
        @SerializedName("redirectionUrl")
        val redirectionUrl: String? = "",
        val downloadEnabled: Boolean = false,
        val offlinePlaybackEnabled:Boolean = false,
        var order: Int = 0
    ) : Parcelable, Serializable

    @Parcelize
    data class Artist(
        @SerializedName("id")
        val id: Int,
        @SerializedName("name")
        val name: String = "",
        @SerializedName("description")
        val description: String? = null,
        @SerializedName("imageUrl")
        val imageURL: String? = null
    ) : Parcelable

    @Parcelize
    data class MediaFileInfo(
        @SerializedName("id")
        val id: Long = -1,
        @SerializedName("name")
        val name: String = "",
        @SerializedName("order")
        val order: Int = -1,
        @SerializedName("mediaFile")
        val mediaFile: MediaFile
    ) : Parcelable

    @Parcelize
    data class MediaFile(
        val id: String = "",
        @SerializedName("name")
        val name: String = "",
        @SerializedName("fileKey")
        val trackKey: String = "",
        @SerializedName("order")
        val order: Int = -1,
        @SerializedName("mediaFileSourceType")
        var sourceType: String,
        @SerializedName("fileUrl")
        val fileUrl: String,
        @SerializedName("fileType")
        val fileType: String?,
        @SerializedName("fileMetadata")
        @Ignore
        val metadataString: String? = null,
        @SerializedName("fileElements")
        val fileElements: List<MediaFile>? = emptyList()
    ) : Parcelable {

        @IgnoredOnParcel
        private var deserialized: TrackMetadata? = null
        val trackMetadata: TrackMetadata
            get() {
                if (metadataString == null) return TrackMetadata(0.0, "audio", "audio")
                return if (deserialized != null) {
                    deserialized!!
                } else {

                    deserialized = Gson().fromJson(
                        metadataString,
                        TrackMetadata::class.java
                    )
                    deserialized!!
                }
            }

        val isVideo: Boolean
            get() {
                return trackMetadata.mediaType.contains("video")
            }
    }

    @Parcelize
    data class LiveShow(
        val id: String,
        val startDate: Long,
        val endDate: Long,
        val title: String?,
        val description: String?,
        val useArtistDetails: Boolean,
        val coverImageUrl: String,
        val artist: Artist?,
        @SerializedName("hideDatesOnClients")
        val hideDates : Boolean? = false,
        val liveStream: LiveStream?,
        var liveShowState: String? = null,
        val donateLink: String? = null,
        val donateMessage: String? = null,
        val archiveData: List<ArchiveData> = emptyList()
    ) : Parcelable, Serializable {
        var chatEnabled: Boolean? = false
        fun fetchTitle() = (if (useArtistDetails) artist?.name else title) ?: ""
        fun fetchDescription() = (if (useArtistDetails) artist?.description else description) ?: ""

        fun isDonateType() = donateLink != null

        fun order() : Int{
            return when (LiveShowState.getState(liveShowState)) {
                LiveShowState.Live -> 1
                LiveShowState.Upcoming -> 2
                LiveShowState.Archived, LiveShowState.Archiving -> 3
                LiveShowState.Expired -> 4
                null -> 0
            }
        }
    }

    @Parcelize
    data class LiveStream(
        val id: String,
        val title: String,
        val outputDestinationUrl: String,
        val liveStreamType: LiveStreamType?,
        val streamingFile: MediaFile? = null
    ) : Parcelable

    @Parcelize
    data class ArchiveData(
        val id: String,
        val archiveUrl: String,
        val archivedMediaFile: MediaFile
    ) : Parcelable

    @Parcelize
    data class TrackMetadata(
        @SerializedName("DURATION") val duration: Double,
        @SerializedName("MEDIATYPE") val mediaType: String,
        @SerializedName("TYPE") val type: String?,
        @SerializedName("CODEC") val codec: String? = null,
        @SerializedName("SONY") val sonyEnabled: Boolean? = null,
        @SerializedName("AURO") val auroEnabled: Boolean? = null,
        @SerializedName("MQA") val mqaEnabled: Boolean? = null
    ) : Parcelable

    sealed class LiveShowState(val state: String) {
        object Live : LiveShowState("LIVE")
        object Upcoming : LiveShowState("UPCOMING")
        object Archived : LiveShowState("ARCHIVED")
        object Archiving : LiveShowState("ARCHIVING")
        object Expired : LiveShowState("EXPIRED")

        companion object {
            fun getState(state: String?): LiveShowState? {
                return when (state) {
                    Live.state -> Live
                    Upcoming.state -> Upcoming
                    Archived.state -> Archived
                    Archiving.state -> Archiving
                    else -> null
                }
            }
        }
    }

    enum class LiveStreamType {
        AWS,
        EXTERNAL,
        MEDIA_LIBRARY,
        EXTERNAL_WITH_S3
    }
}