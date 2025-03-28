package com.example.playersampleapp

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

class PlayerSampleApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@PlayerSampleApplication)
            modules(appModule(this@PlayerSampleApplication, defaultDownloaderConfig(applicationContext)))
        }
    }
}