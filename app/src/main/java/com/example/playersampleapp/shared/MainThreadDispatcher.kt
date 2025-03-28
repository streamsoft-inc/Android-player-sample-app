package com.example.playersampleapp.shared

import android.os.Handler
import android.os.Looper

object MainThreadDispatcher {

    private val handler: Handler by lazy { Handler(Looper.getMainLooper()) }

    fun post(command: Runnable) {
        handler.post(command)
    }

    fun postDelayed(command: Runnable, delay: Long) {
        handler.postDelayed(command, delay)
    }
}