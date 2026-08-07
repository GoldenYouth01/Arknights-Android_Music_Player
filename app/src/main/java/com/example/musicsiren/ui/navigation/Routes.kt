package com.example.musicsiren.ui.navigation

object Routes {
    const val ALBUMS = "albums"
    const val SEARCH = "search"
    const val PLAYLISTS = "playlists"
    const val DOWNLOADS = "downloads"
    const val ALBUM_DETAIL = "album/{cid}"
    const val PLAYLIST_DETAIL = "playlist/{id}"

    fun albumDetail(cid: String) = "album/$cid"
    fun playlistDetail(id: String) = "playlist/$id"
}
