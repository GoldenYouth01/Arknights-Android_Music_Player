package com.example.musicsiren.domain.model

data class Song(
    val cid: String,
    val name: String,
    val albumCid: String?,
    val artists: List<String>,
    val sourceUrl: String?,
    val lyricUrl: String?,
)
