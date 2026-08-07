package com.example.musicsiren

import android.app.Application
import com.example.musicsiren.di.AppContainer

class MusicApp : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}
