package com.example.musicsiren.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.musicsiren.MusicApp
import com.example.musicsiren.data.repository.DownloadRepository
import okhttp3.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * 后台下载 WAV（每首约 54 MB）。
 * 失败按指数退避重试，最多 [MAX_ATTEMPTS] 次；CDN 链接失效时经 /api/song/{cid} 重新解析。
 */
class DownloadWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val cid = inputData.getString(KEY_CID) ?: return Result.failure()
        val container = (applicationContext as MusicApp).container
        val repo = container.downloadRepository

        val record = repo.recordFor(cid) ?: return Result.failure()
        val sourceUrl = record.sourceUrl.ifBlank {
            runCatching { container.sirenRepository.getSongUrl(cid) }.getOrNull() ?: return Result.failure()
        }

        val dir = File(applicationContext.filesDir, "downloads").apply { mkdirs() }
        val target = File(dir, sanitize(record.songCid) + ".wav")

        return try {
            // 空间不足则快速失败
            val free = repo.availableBytes()
            if (free in 1 until (ESTIMATED_BYTES + MARGIN)) {
                repo.markError(cid, "存储空间不足")
                return Result.failure()
            }

            var response: Response? = null
            try {
                response = execute(sourceUrl)
                if (!response.isSuccessful) {
                    // CDN 链接可能过期：经 API 重新解析一次
                    response.close()
                    response = null
                    val freshUrl = container.sirenRepository.getSongUrl(cid)
                    if (freshUrl.isNullOrBlank()) return retryOrFail(repo, cid)
                    response = execute(freshUrl)
                    if (!response.isSuccessful) return retryOrFail(repo, cid)
                }
                val total = response.body?.contentLength() ?: -1L
                response.body!!.byteStream().use { input ->
                    FileOutputStream(target).use { out ->
                        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                        var read: Int
                        var bytes = 0L
                        var lastUpdate = 0L
                        while (input.read(buffer).also { read = it } != -1) {
                            if (isStopped) {
                                target.delete()
                                return Result.retry()
                            }
                            out.write(buffer, 0, read)
                            bytes += read
                            if (bytes - lastUpdate >= PROGRESS_STEP) {
                                lastUpdate = bytes
                                repo.updateProgress(cid, bytes, if (total > 0) total else bytes + PROGRESS_STEP)
                                setProgress(
                                    workDataOf(PROGRESS to bytes, TOTAL to total)
                                )
                            }
                        }
                    }
                }
            } finally {
                response?.close()
            }

            repo.markDownloaded(cid, target.absolutePath, target.length())
            Result.success()
        } catch (e: IOException) {
            target.delete()
            retryOrFail(repo, cid, e.message)
        } catch (e: Exception) {
            target.delete()
            repo.markError(cid, e.message)
            Result.failure()
        }
    }

    private fun execute(url: String): Response =
        containerOkHttpClient().newCall(okhttp3.Request.Builder().url(url).build()).execute()

    private fun containerOkHttpClient() = (applicationContext as MusicApp).container.okHttpClient

    private suspend fun retryOrFail(repo: DownloadRepository, cid: String, message: String? = null): Result =
        if (runAttemptCount < MAX_ATTEMPTS - 1) {
            Result.retry()
        } else {
            repo.markError(cid, message ?: "下载失败")
            Result.failure()
        }

    companion object {
        const val KEY_CID = "cid"
        const val PROGRESS = "progress"
        const val TOTAL = "total"
        private const val PROGRESS_STEP = 1_048_576L // 每 1MB 更新一次进度
        private const val DEFAULT_BUFFER_SIZE = 256 * 1024
        private const val MAX_ATTEMPTS = 3
        private const val ESTIMATED_BYTES = 60L * 1024 * 1024
        private const val MARGIN = 50L * 1024 * 1024

        fun sanitize(name: String): String = name.replace(Regex("[^A-Za-z0-9_-]"), "_")
    }
}
