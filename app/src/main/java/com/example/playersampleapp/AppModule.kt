package com.example.playersampleapp

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.DatabaseProvider
import androidx.media3.database.DefaultDatabaseProvider
import androidx.media3.database.ExoDatabaseProvider
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.ExoPlayer
import com.example.playersampleapp.extension.isOnTV
import com.example.playersampleapp.server.AppMediaPlayer
import com.example.playersampleapp.server.ConnectController
import com.example.playersampleapp.server.EmbeddedMediaHttpServer
import com.example.playersampleapp.server.NSDController
import com.example.playersampleapp.server.model.FileDownloaderConfig
import com.example.playersampleapp.server.model.basePlayerAPIEndpoint
import com.example.playersampleapp.viewModel.PlayerViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

const val MOBILE_CACHE_SIZE = 10 * 1024 * 1024 * 1024L
const val TV_CACHE_SIZE = 4 * 1024 * 1024 * 1024L

@OptIn(UnstableApi::class)
fun appModule(appContext: Context, fileDownloaderConfig: FileDownloaderConfig) = module {

    single { ExoPlayer.Builder(get()).build() }
    viewModel { PlayerViewModel(get(), get()) }

    single<DatabaseProvider> {
        DefaultDatabaseProvider(StandaloneDatabaseProvider(appContext))
    }

    // NEED TO HAVE ONLY ONE INSTANCE OF SIMPLE CACHE, SO IT MUST BE IN MODULE WHICH LIVES AS LONG AS APP
    single<Cache> {
        SimpleCache(
            fileDownloaderConfig.downloadDirectory,
            LeastRecentlyUsedCacheEvictor(if (appContext.isOnTV()) TV_CACHE_SIZE else MOBILE_CACHE_SIZE),
            get<DatabaseProvider>()
        )
    }

    single {
        NSDController()
    }
    single<ConnectController> {

        ConnectController(
            EmbeddedMediaHttpServer(basePlayerAPIEndpoint),
            get(),
            get(),get()
        )
    }
}