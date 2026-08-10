package com.example.musicsiren.domain.model

import kotlinx.serialization.Serializable

/** 登录会话（AuthStore 持久化）。token 为 JWT，泄露至 logcat 后即视为风险，仅存本地。 */
@Serializable
data class AuthSession(
    val token: String,
    val userId: Int,
    val email: String,
    val nickname: String? = null,
)
