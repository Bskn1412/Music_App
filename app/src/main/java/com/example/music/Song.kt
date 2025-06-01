package com.example.music

data class Song(
    val title: String,
    val path: String,
    val iconResId: Int = R.drawable.ic_music // default value
)
