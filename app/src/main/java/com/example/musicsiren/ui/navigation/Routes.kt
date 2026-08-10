package com.example.musicsiren.ui.navigation

object Routes {
    const val ALBUMS = "albums"
    const val SEARCH = "search"
    const val PLAYLISTS = "playlists"
    const val DOWNLOADS = "downloads"
    const val ACCOUNT = "account"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FORGOT = "forgot"
    const val CLOUD_PLAYLISTS = "cloud/playlists"
    const val CLOUD_HISTORY = "cloud/history"
    const val ALBUM_DETAIL = "album/{cid}"
    const val PLAYLIST_DETAIL = "playlist/{id}"

    fun albumDetail(cid: String) = "album/$cid"
    fun playlistDetail(id: String) = "playlist/$id"
}
