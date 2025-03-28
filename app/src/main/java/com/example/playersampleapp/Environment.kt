package com.example.playersampleapp

import android.content.Context
import com.example.playersampleapp.server.model.FileDownloaderConfig

fun defaultDownloaderConfig(appContext: Context) = FileDownloaderConfig(
    downloadDirectory = appContext.getExternalFilesDir(null) ?: appContext.filesDir
)