package com.example.musicsiren.data.repository

import com.example.musicsiren.data.remote.ApiResponse
import com.example.musicsiren.data.remote.EmptyData

/** 云端业务错误（body.code != 0）。code 见后端约定：1001~1011。 */
class CloudApiException(val code: Int, override val message: String) : Exception(message)

/** 解包云端响应：code != 0 抛 CloudApiException；data 缺失同样视为错误。 */
internal fun <T> ApiResponse<T>.cloudDataOrThrow(): T {
    if (code != 0) throw CloudApiException(code, msg ?: "请求失败")
    return data ?: throw CloudApiException(code, "响应数据为空")
}

/** 无内容端点（data:{} 的 EmptyData）：仅检查 code。 */
internal fun ApiResponse<EmptyData>.cloudDataOrThrowEmpty() {
    if (code != 0) throw CloudApiException(code, msg ?: "请求失败")
}
