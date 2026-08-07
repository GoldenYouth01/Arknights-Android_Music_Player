package com.example.musicsiren.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.musicsiren.R
import com.example.musicsiren.di.AppContainer
import com.example.musicsiren.ui.UiState
import com.example.musicsiren.ui.components.AlbumRow
import com.example.musicsiren.ui.components.ErrorState
import com.example.musicsiren.ui.components.HairlineDivider
import com.example.musicsiren.ui.components.LoadingBox
import com.example.musicsiren.ui.theme.AccentCyan
import com.example.musicsiren.ui.theme.Background
import com.example.musicsiren.ui.theme.SirenType
import com.example.musicsiren.ui.theme.TextMuted
import com.example.musicsiren.ui.theme.TextSecondary

/**
 * 专辑列表（首页）：竖向行列表 + 下拉刷新 + 顶栏刷新按钮 —— 数据随时更新。
 */
@Composable
fun AlbumListScreen(
    container: AppContainer,
    onAlbumClick: (String) -> Unit,
    viewModel: AlbumListViewModel = viewModel { AlbumListViewModel(container.sirenRepository) },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize().background(Background)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text("塞壬唱片", style = SirenType.DisplaySerif, color = AccentCyan)
                Spacer(Modifier.height(2.dp))
                Text("MONSTER SIREN RECORDS", style = SirenType.Label, color = TextMuted)
            }
            IconButton(onClick = { viewModel.refresh() }) {
                Icon(Icons.Default.Refresh, contentDescription = "刷新", tint = TextSecondary)
            }
        }
        HairlineDivider()

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier.fillMaxSize(),
        ) {
            when (val state = uiState) {
                UiState.Loading -> LoadingBox()
                is UiState.Error -> ErrorState(state.message, onRetry = { viewModel.load() })
                is UiState.Data -> {
                    if (state.data.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = stringResource(R.string.empty_albums),
                                style = SirenType.Body,
                                color = TextSecondary,
                            )
                        }
                    } else {
                        LazyColumn {
                            items(state.data, key = { it.cid }) { album ->
                                AlbumRow(album, onClick = { onAlbumClick(album.cid) })
                                HairlineDivider()
                            }
                        }
                    }
                }
            }
        }
    }
}
