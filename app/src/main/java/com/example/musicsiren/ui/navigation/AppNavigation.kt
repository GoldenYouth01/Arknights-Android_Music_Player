package com.example.musicsiren.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.musicsiren.di.AppContainer
import com.example.musicsiren.playback.PlaybackViewModel
import com.example.musicsiren.ui.components.AppDownload
import com.example.musicsiren.ui.components.AppQueueMusic
import com.example.musicsiren.ui.components.PlayerBar
import com.example.musicsiren.ui.components.PlayerDrawer
import com.example.musicsiren.ui.screens.AccountScreen
import com.example.musicsiren.ui.screens.AlbumDetailScreen
import com.example.musicsiren.ui.screens.AlbumListScreen
import com.example.musicsiren.ui.screens.CloudPlaylistsScreen
import com.example.musicsiren.ui.screens.DownloadsScreen
import com.example.musicsiren.ui.screens.ForgotPasswordScreen
import com.example.musicsiren.ui.screens.HistoryScreen
import com.example.musicsiren.ui.screens.LoginScreen
import com.example.musicsiren.ui.screens.LyricsViewModel
import com.example.musicsiren.ui.screens.NowPlayingScreen
import com.example.musicsiren.ui.screens.PlaylistDetailScreen
import com.example.musicsiren.ui.screens.PlaylistsScreen
import com.example.musicsiren.ui.screens.RegisterScreen
import com.example.musicsiren.ui.screens.SearchScreen
import com.example.musicsiren.ui.theme.AccentCyan
import com.example.musicsiren.ui.theme.Background
import com.example.musicsiren.ui.theme.SirenType
import com.example.musicsiren.ui.theme.SurfaceDark
import com.example.musicsiren.ui.theme.TextSecondary

/**
 * App 根：Scaffold（底部 = 播放条 + 底部导航）+ NavHost + 全屏播放页 overlay + 队列抽屉 overlay。
 */
@Composable
fun MusicAppRoot(container: AppContainer, playbackViewModel: PlaybackViewModel) {
    val navController = rememberNavController()
    val lyricsViewModel: LyricsViewModel = viewModel {
        LyricsViewModel(container.lyricsRepository, playbackViewModel)
    }
    val uiState by playbackViewModel.uiState.collectAsStateWithLifecycle()
    val nowPlayingVisible by playbackViewModel.nowPlayingVisible.collectAsStateWithLifecycle()
    val drawerVisible by playbackViewModel.drawerVisible.collectAsStateWithLifecycle()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Box(Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Background,
            bottomBar = {
                Column(Modifier.navigationBarsPadding()) {
                    if (uiState.hasCurrent) {
                        PlayerBar(
                            state = uiState,
                            onTap = { playbackViewModel.showNowPlaying() },
                            onTogglePlay = { playbackViewModel.togglePlay() },
                            onNext = { playbackViewModel.next() },
                            onOpenDrawer = { playbackViewModel.showDrawer() },
                            onToggleShuffle = { playbackViewModel.toggleShuffle() },
                            onCycleRepeat = { playbackViewModel.cycleRepeatMode() },
                        )
                    }
                    if (currentRoute in setOf(Routes.ALBUMS, Routes.SEARCH, Routes.PLAYLISTS, Routes.DOWNLOADS, Routes.ACCOUNT)) {
                        SirenNavBar(
                            currentRoute = currentRoute,
                            onSelect = { route ->
                                navController.navigate(route) {
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                        )
                    }
                }
            },
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = Routes.ALBUMS,
                modifier = Modifier.padding(padding),
            ) {
                composable(Routes.ALBUMS) {
                    AlbumListScreen(
                        container = container,
                        onAlbumClick = { cid -> navController.navigate(Routes.albumDetail(cid)) },
                    )
                }
                composable(Routes.SEARCH) {
                    SearchScreen(
                        container = container,
                        playbackViewModel = playbackViewModel,
                        onAlbumClick = { cid -> navController.navigate(Routes.albumDetail(cid)) },
                    )
                }
                composable(Routes.PLAYLISTS) {
                    PlaylistsScreen(
                        container = container,
                        onPlaylistClick = { id -> navController.navigate(Routes.playlistDetail(id)) },
                    )
                }
                composable(Routes.DOWNLOADS) {
                    DownloadsScreen(
                        container = container,
                        playbackViewModel = playbackViewModel,
                    )
                }
                composable(Routes.ACCOUNT) {
                    AccountScreen(
                        container = container,
                        onNavigateLogin = { navController.navigate(Routes.LOGIN) },
                        onNavigateRegister = { navController.navigate(Routes.REGISTER) },
                        onNavigateCloudPlaylists = { navController.navigate(Routes.CLOUD_PLAYLISTS) },
                        onNavigateHistory = { navController.navigate(Routes.CLOUD_HISTORY) },
                    )
                }
                composable(Routes.LOGIN) {
                    LoginScreen(
                        container = container,
                        onBack = { navController.popBackStack() },
                        onNavigateRegister = { navController.navigate(Routes.REGISTER) },
                        onNavigateForgot = { navController.navigate(Routes.FORGOT) },
                    )
                }
                composable(Routes.REGISTER) {
                    RegisterScreen(
                        container = container,
                        onBack = { navController.popBackStack() },
                    )
                }
                composable(Routes.FORGOT) {
                    ForgotPasswordScreen(
                        container = container,
                        onBack = { navController.popBackStack() },
                    )
                }
                composable(Routes.CLOUD_PLAYLISTS) {
                    CloudPlaylistsScreen(
                        container = container,
                        onBack = { navController.popBackStack() },
                        onPlaylistClick = { id -> navController.navigate(Routes.playlistDetail(id)) },
                    )
                }
                composable(Routes.CLOUD_HISTORY) {
                    HistoryScreen(
                        container = container,
                        playbackViewModel = playbackViewModel,
                        onBack = { navController.popBackStack() },
                    )
                }
                composable(Routes.ALBUM_DETAIL) { entry ->
                    AlbumDetailScreen(
                        cid = entry.arguments?.getString("cid") ?: "",
                        container = container,
                        playbackViewModel = playbackViewModel,
                        onBack = { navController.popBackStack() },
                    )
                }
                composable(Routes.PLAYLIST_DETAIL) { entry ->
                    PlaylistDetailScreen(
                        playlistId = entry.arguments?.getString("id") ?: "",
                        container = container,
                        playbackViewModel = playbackViewModel,
                        onBack = { navController.popBackStack() },
                        onNavigateLogin = { navController.navigate(Routes.LOGIN) },
                    )
                }
            }
        }

        // 全屏播放页 overlay（压在导航之上，返回键关闭）
        AnimatedVisibility(
            visible = nowPlayingVisible,
            enter = slideInVertically { it },
            exit = slideOutVertically { it },
        ) {
            NowPlayingScreen(
                state = uiState,
                lyricsViewModel = lyricsViewModel,
                onBack = { playbackViewModel.hideNowPlaying() },
                onTogglePlay = { playbackViewModel.togglePlay() },
                onNext = { playbackViewModel.next() },
                onPrevious = { playbackViewModel.previous() },
                onSeek = { playbackViewModel.seekTo(it) },
                onToggleShuffle = { playbackViewModel.toggleShuffle() },
                onCycleRepeat = { playbackViewModel.cycleRepeatMode() },
                modifier = Modifier.fillMaxSize(),
            )
        }

        // 播放队列抽屉
        AnimatedVisibility(
            visible = drawerVisible,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            PlayerDrawer(
                state = uiState,
                onDismiss = { playbackViewModel.hideDrawer() },
                onSelectSong = { index -> playbackViewModel.seekToIndex(index) },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun SirenNavBar(currentRoute: String?, onSelect: (String) -> Unit) {
    Surface(color = SurfaceDark, tonalElevation = 0.dp) {
        Row(Modifier.fillMaxWidth()) {
            NavItem(
                label = "专辑",
                icon = Icons.Default.Home,
                route = Routes.ALBUMS,
                selected = currentRoute == Routes.ALBUMS,
                onClick = { onSelect(Routes.ALBUMS) },
                modifier = Modifier.weight(1f),
            )
            NavItem(
                label = "搜索",
                icon = Icons.Default.Search,
                route = Routes.SEARCH,
                selected = currentRoute == Routes.SEARCH,
                onClick = { onSelect(Routes.SEARCH) },
                modifier = Modifier.weight(1f),
            )
            NavItem(
                label = "歌单",
                icon = AppQueueMusic,
                route = Routes.PLAYLISTS,
                selected = currentRoute == Routes.PLAYLISTS,
                onClick = { onSelect(Routes.PLAYLISTS) },
                modifier = Modifier.weight(1f),
            )
            NavItem(
                label = "下载",
                icon = AppDownload,
                route = Routes.DOWNLOADS,
                selected = currentRoute == Routes.DOWNLOADS,
                onClick = { onSelect(Routes.DOWNLOADS) },
                modifier = Modifier.weight(1f),
            )
            NavItem(
                label = "账号",
                icon = Icons.Default.Person,
                route = Routes.ACCOUNT,
                selected = currentRoute == Routes.ACCOUNT,
                onClick = { onSelect(Routes.ACCOUNT) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun NavItem(
    label: String,
    icon: ImageVector,
    route: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val color = if (selected) AccentCyan else TextSecondary
    Column(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(22.dp))
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            style = SirenType.Label,
            color = color,
        )
    }
}
