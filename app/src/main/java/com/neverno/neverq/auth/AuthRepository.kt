package com.neverno.neverq.auth

import com.neverno.neverq.core.data.TokenManager
import com.neverno.neverq.core.models.LoginRequest
import com.neverno.neverq.core.models.LoginResponse
import com.neverno.neverq.core.network.ApiService
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
) {
    suspend fun login(email: String, password: String): AuthResult {
        return try {
            val response = api.login(LoginRequest(email.trim(), password))
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                tokenManager.saveSession(
                    accessToken = body.access,
                    refreshToken = body.refresh,
                    userType = body.userType,
                    role = body.role,
                    name = body.name,
                    email = body.email,
                    companyId = body.companyId,
                )
                AuthResult.Success(body)
            } else {
                val msg = when (response.code()) {
                    401 -> "Invalid email or password."
                    403 -> "Account pending approval."
                    else -> "Login failed. Please try again."
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
}
