package com.example.musicsiren.ui.navigation

object Routes {
    const val ALBUMS = "albums"
    const val SEARCH = "search"
    const val DOWNLOADS = "downloads"
    const val ALBUM_DETAIL = "album/{cid}"

    fun albumDetail(cid: String) = "album/$cid"
}
