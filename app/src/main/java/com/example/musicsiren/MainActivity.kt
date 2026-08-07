package com.example.musicsiren

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.musicsiren.playback.PlaybackViewModel
import com.example.musicsiren.ui.navigation.MusicAppRoot
import com.example.musicsiren.ui.theme.SirenTheme

class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* 忽略结果 */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // API 33+ 首次启动请求通知权限（媒体通知需要）
        if (Build.VERSION.SDK_INT >= 33) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        setContent {
            SirenTheme {
                val container = (application as MusicApp).container
                val playbackViewModel: PlaybackViewModel = viewModel {
                    PlaybackViewModel(
                        application = application,
                        sirenRepository = container.sirenRepository,
                        downloadRepository = container.downloadRepository,
                    )
                }
                MusicAppRoot(container = container, playbackViewModel = playbackViewModel)
            }
        }
    }
}
