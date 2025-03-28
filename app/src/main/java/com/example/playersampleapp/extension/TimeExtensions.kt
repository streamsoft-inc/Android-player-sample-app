package com.example.playersampleapp.extension

fun String.durationToTime() : Long {
    val items = this.split(":")
    var seconds = 0L
    try {
        if (items.size == 3) {
            seconds = (Integer.parseInt(items[2]) +
                    Integer.parseInt(items[1]) * 60).toLong() +
                    Integer.parseInt(items[0]) * 60 * 60L
        } else if (items.size == 2) {
            seconds =  Integer.parseInt(items[1]) +
                    Integer.parseInt(items[0]) * 60L
        }
    } catch (e: java.lang.NumberFormatException) {
        e.printStackTrace()
    }
    return seconds
}