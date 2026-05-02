package com.neverno.neverq.core.network

import com.neverno.neverq.core.data.TokenManager
import com.neverno.neverq.core.models.TokenRefreshRequest
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(private val tokenManager: TokenManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val path = chain.request().url.encodedPath
        if (path.contains("/auth/login/") || path.contains("/auth/token/refresh/")) {
            return chain.proceed(chain.request())
        }

        val token = runBlocking { tokenManager.getAccessTokenNow() }
        val request = chain.request().newBuilder().apply {
            if (token != null) header("Authorization", "Bearer $token")
        }.build()
        return chain.proceed(request)
    }
}

@Singleton
class TokenRefreshInterceptor @Inject constructor(
    private val tokenManager: TokenManager,
    private val apiService: dagger.Lazy<ApiService>,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        val path = chain.request().url.encodedPath
        if (path.contains("/auth/login/") || path.contains("/auth/token/refresh/")) {
            return response
        }

        if (response.code == 401) {
            response.close()
            val newToken = runBlocking {
                val refresh = tokenManager.getRefreshTokenNow() ?: return@runBlocking null
                val result = apiService.get().refreshToken(TokenRefreshRequest(refresh))
                if (result.isSuccessful) {
                    val body = result.body()
                    body?.access?.also { tokenManager.updateAccessToken(it) }
                } else {
                    tokenManager.clearSession()
                    null
                }
            }
            if (newToken != null) {
                val newRequest = chain.request().newBuilder()
                    .header("Authorization", "Bearer $newToken")
                    .build()
                return chain.proceed(newRequest)
            }
        }
        return response
    }
}
