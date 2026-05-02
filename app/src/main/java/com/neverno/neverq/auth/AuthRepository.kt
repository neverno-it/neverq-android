package com.neverno.neverq.auth

import com.neverno.neverq.core.data.TokenManager
import com.neverno.neverq.core.models.LoginRequest
import com.neverno.neverq.core.models.LoginResponse
import com.neverno.neverq.core.network.ApiService
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import javax.inject.Inject
import javax.inject.Singleton

sealed class AuthResult {
    data class Success(val response: LoginResponse) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

@Singleton
class AuthRepository @Inject constructor(
    private val api: ApiService,
    private val tokenManager: TokenManager,
    private val moshi: Moshi,
) {
    suspend fun login(email: String, password: String, userType: String): AuthResult {
        return try {
            val response = api.login(LoginRequest(email.trim(), password, userType))
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                val resolvedUserType = body.userType.ifBlank { userType }
                tokenManager.saveSession(
                    accessToken = body.access,
                    refreshToken = body.refresh,
                    userType = resolvedUserType,
                    role = body.role.orEmpty(),
                    name = body.name.ifBlank { email.trim() },
                    email = body.email.ifBlank { email.trim() },
                    companyId = body.companyId,
                )
                AuthResult.Success(body.copy(userType = resolvedUserType))
            } else {
                val msg = when (response.code()) {
                    401 -> "Invalid email or password."
                    403 -> "Account pending approval."
                    else -> response.errorBody()?.string()?.toApiErrorMessage() ?: "Login failed. Please try again."
                }
                AuthResult.Error(msg)
            }
        } catch (e: Exception) {
            AuthResult.Error("Network error: ${e.localizedMessage}")
        }
    }

    suspend fun logout() {
        try {
            val refresh = tokenManager.getRefreshTokenNow()
            if (refresh != null) api.logout(mapOf("refresh" to refresh))
        } catch (_: Exception) {}
        tokenManager.clearSession()
    }

    suspend fun isLoggedIn() = tokenManager.isLoggedIn()
    suspend fun getUserRole() = tokenManager.getUserRoleNow()
    suspend fun getUserType() = tokenManager.getUserTypeNow()

    private fun String.toApiErrorMessage(): String? {
        return try {
            val type = Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
            val adapter = moshi.adapter<Map<String, Any?>>(type)
            val parsed = adapter.fromJson(this)
            parsed?.get("detail")?.toString()
                ?: parsed?.get("error")?.toString()
                ?: parsed?.get("message")?.toString()
        } catch (_: Exception) {
            null
        }
    }
}
