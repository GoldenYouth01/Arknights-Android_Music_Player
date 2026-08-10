package com.example.musicsiren.data.remote

/**
 * 内存态 token 提供者：OkHttp 鉴权拦截器在非挂起上下文同步读取。
 * 由 AuthRepository 在加载会话 / 登录 / 登出时维护。
 */
class AuthTokenProvider {
    @Volatile
    var token: String? = null
}
