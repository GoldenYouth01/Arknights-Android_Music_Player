package com.example.musicsiren.data.repository

import android.content.Context
import android.os.Environment
import android.os.StatFs
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.musicsiren.data.local.DownloadStore
import com.example.musicsiren.domain.model.DownloadRecord
import com.example.musicsiren.domain.model.DownloadStatus
import com.example.musicsiren.work.DownloadWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * 下载的单一事实源：内存 StateFlow 提供实时进度，DataStore 持久化状态。
 */
class DownloadRepository(
    private val appContext: Context,
    private val store: DownloadStore,
    private val scope: CoroutineScope,
) {
    private val _records = MutableStateFlow<Map<String, DownloadRecord>>(emptyMap())
    val records: StateFlow<Map<String, DownloadRecord>> = _records.asStateFlow()

    init {
        scope.launch(Dispatchers.IO) {
            store.records.collect { list ->
                _records.value = list.associateBy { it.songCid }
            }
        }
    }

    fun recordFor(cid: String): DownloadRecord? = _records.value[cid]

    fun isDownloaded(cid: String): Boolean = recordFor(cid)?.localPath != null

    fun localPathFor(cid: String): String? = recordFor(cid)?.localPath

    private suspend fun persist(record: DownloadRecord) {
        _records.value = _records.value + (record.songCid to record)
        store.saveAll(_records.value.values.toList())
    }

    fun enqueueDownload(record: DownloadRecord) {
        scope.launch(Dispatchers.IO) {
            persist(record.copy(status = DownloadStatus.PENDING))
        }
        val request = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setInputData(workDataOf(DownloadWorker.KEY_CID to record.songCid))
            .setConstraints(Constraints(NetworkType.CONNECTED))
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
            .build()
        WorkManager.getInstance(appContext)
            .enqueueUniqueWork("download-${record.songCid}", ExistingWorkPolicy.APPEND_OR_REPLACE, request)
    }

    suspend fun updateProgress(cid: String, progressBytes: Long, totalBytes: Long) {
        val current = recordFor(cid) ?: return
        persist(
            current.copy(
                status = DownloadStatus.DOWNLOADING,
                progressBytes = progressBytes,
                totalBytes = if (totalBytes > 0) totalBytes else current.totalBytes,
            )
        )
    }

    suspend fun markDownloaded(cid: String, localPath: String, size: Long) {
        val current = recordFor(cid) ?: return
        persist(
            current.copy(
                status = DownloadStatus.DOWNLOADED,
                localPath = localPath,
                progressBytes = size,
                totalBytes = size,
                error = null,
                completedAt = System.currentTimeMillis(),
            )
        )
    }

    suspend fun markError(cid: String, message: String?) {
        val current = recordFor(cid) ?: return
        persist(current.copy(status = DownloadStatus.ERROR, error = message))
    }

    fun cancelAndDelete(cid: String) {
        WorkManager.getInstance(appContext).cancelUniqueWork("download-$cid")
        val record = recordFor(cid)
        scope.launch(Dispatchers.IO) {
            record?.localPath?.let { path -> runCatching { File(path).delete() } }
            _records.value = _records.value - cid
            store.saveAll(_records.value.values.toList())
        }
    }

    /** 设备可用存储字节数（-1 表示未知）。 */
    fun availableBytes(): Long = runCatching {
        StatFs(Environment.getDataDirectory().absolutePath).availableBytes
    }.getOrDefault(-1L)

    private companion object {
        const val ESTIMATED_BYTES_PER_TRACK = 60L * 1024 * 1024
    }
}
