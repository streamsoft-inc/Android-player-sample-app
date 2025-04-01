package com.example.playersampleapp.server

import android.os.Bundle
import com.example.playersampleapp.model.Models

class PlaybackFragmentArgs(var playlist: Models.Album? = null, var index: Int = -1, var liveShow: Models.LiveShow? = null, var preloaded:Boolean = false) {

    companion object {
        fun fromBundle(bundle: Bundle?) : PlaybackFragmentArgs {
            val index = bundle?.getInt("mediaFileInfo", -1) ?: -1
            val album: Models.Album? = bundle?.getParcelable("playlist")
            val live: Models.LiveShow? = bundle?.getParcelable("liveshow")
            val preloaded: Boolean = bundle?.getBoolean("preloaded") ?: false
            return PlaybackFragmentArgs(album, index, live, preloaded)
        }

    }

    fun hasData() = this.index != -1 && this.playlist != null
    fun livePlayback() = this.liveShow != null
    fun preloaded() = this.preloaded

    fun toBundle(): Bundle {
        return Bundle().apply {
            putInt("mediaFileInfo", index)
            putBoolean("preloaded", preloaded)
            putParcelable("playlist", playlist)
            putParcelable("liveshow", liveShow)
        }
    }
}