package com.example.musicsiren.ui.screens

import androidx.lifecycle.ViewModel
import com.example.musicsiren.data.repository.DownloadRepository
import com.example.musicsiren.domain.model.DownloadRecord
import com.example.musicsiren.domain.model.DownloadStatus
import kotlinx.coroutines.flow.StateFlow

class DownloadsViewModel(
    private val downloadRepository: DownloadRepository,
) : ViewModel() {

    val records: StateFlow<Map<String, DownloadRecord>> = downloadRepository.records

    fun delete(cid: String) = downloadRepository.cancelAndDelete(cid)

    fun retry(record: DownloadRecord) {
        downloadRepository.enqueueDownload(
            record.copy(status = DownloadStatus.PENDING, error = null)
        )
    }
}
