package com.example.musicsiren.domain.model

data class Album(
    val cid: String,
    val name: String,
    val coverUrl: String?,
    val coverDeUrl: String?,
    val artistes: List<String>,
)
