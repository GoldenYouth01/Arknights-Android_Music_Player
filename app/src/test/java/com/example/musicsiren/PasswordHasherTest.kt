package com.example.musicsiren

import com.example.musicsiren.util.PasswordHasher
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * 客户端密码哈希契约测试。已知向量与 PHP 端 hash('sha256', 'musicsiren_v1:'.$plain) 一致，
 * 即与服务端校验的 passwordHash 格式（64 位 hex）完全对齐。
 */
class PasswordHasherTest {

    @Test
    fun `known vector secret123`() {
        assertEquals(
            "6facff956818ffd6b7763f63c6c5837c98771fdc19ad1245b8ff14a46ce54791",
            PasswordHasher.hash("secret123"),
        )
    }

    @Test
    fun `known vector 12345678`() {
        assertEquals(
            "c3726b7d4214f3da1f7bd03afade3b5522494587255612150d9fab81842ee495",
            PasswordHasher.hash("12345678"),
        )
    }

    @Test
    fun `empty string vector`() {
        assertEquals(
            "da4f389d73a509d830de8fab3018a5c83ee79d9decdd7ddb6ed70015eab3f614",
            PasswordHasher.hash(""),
        )
    }

    @Test
    fun `output is 64 lowercase hex chars`() {
        val out = PasswordHasher.hash("任意密码·中文")
        assertEquals(64, out.length)
        assertEquals(out, out.lowercase())
        assert(out.all { it.isDigit() || it in 'a'..'f' })
    }

    @Test
    fun `chinese password hashes consistently with utf8 bytes`() {
        val first = PasswordHasher.hash("塞壬唱片")
        val second = PasswordHasher.hash("塞壬唱片")
        assertEquals(first, second)
    }
}
