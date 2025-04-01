package com.example.playersampleapp.shared

import androidx.room.TypeConverter
import com.example.playersampleapp.model.Models
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class AlbumTracksTypeConverter {

    @TypeConverter
    fun fromTracks(gpsPositions: List<Models.MediaFileInfo>): String {
        val gson = Gson()
        val type = object : TypeToken<List<Models.MediaFileInfo>>() {

        }.type
        return gson.toJson(gpsPositions, type)
    }

    @TypeConverter
    fun toTracks(gpsPositions: String): List<Models.MediaFileInfo> {
        val gson = Gson()
        val type = object : TypeToken<List<Models.MediaFileInfo>>() {

        }.type
        return gson.fromJson(gpsPositions, type)
    }

}