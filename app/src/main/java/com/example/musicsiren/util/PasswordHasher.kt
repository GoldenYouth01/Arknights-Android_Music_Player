package com.example.musicsiren.util

import java.security.MessageDigest

/**
 * 客户端密码哈希：SHA-256("musicsiren_v1:" + 明文)（UTF-8，hex 小写）。
 * 与服务端 PHP 契约一致：服务端只存 bcrypt(passwordHash)。明文密码永不进入网络层 / 日志。
 * 前缀带版本号 v1：将来如需升级算法，服务端保留 v1 校验并接受 v2。
 */
object PasswordHasher {
    private const val PREFIX = "musicsiren_v1:"

    fun hash(password: String): String {
        val bytes = (PREFIX + password).toByteArray(Charsets.UTF_8)
        return MessageDigest.getInstance("SHA-256")
            .digest(bytes)
            .joinToString("") { "%02x".format(it) }
    }
}
