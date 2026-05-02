package com.neverno.neverq.auth

import com.neverno.neverq.core.data.TokenManager
import com.neverno.neverq.core.models.GoogleLoginRequest
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
                saveLoginSession(response.body()!!, fallbackUserType = userType, fallbackEmail = email.trim())
            } else {
                val msg = when (response.code()) {
                    401 -> "Invalid email or password."
                    403 -> "Account pending approval."
                    409 -> response.errorBody()?.string()?.toApiErrorMessage() ?: "Please select a customer account in the web portal first."
                    else -> response.errorBody()?.string()?.toApiErrorMessage() ?: "Login failed. Please try again."
                }
                AuthResult.Error(msg)
            }
        } catch (e: Exception) {
            AuthResult.Error("Network error: ${e.localizedMessage}")
        }
    }

    suspend fun googleLogin(idToken: String): AuthResult {
        return try {
            val response = api.googleLogin(GoogleLoginRequest(idToken))
            if (response.isSuccessful && response.body() != null) {
                saveLoginSession(response.body()!!, fallbackUserType = "customer", fallbackEmail = "")
            } else {
                val msg = when (response.code()) {
                    404 -> response.errorBody()?.string()?.toApiErrorMessage()
                        ?: "No customer account exists for this Google email. Please sign up first."
                    409 -> response.errorBody()?.string()?.toApiErrorMessage()
                        ?: "Multiple customer accounts found. Please use email login or select the account in the web portal."
                    else -> response.errorBody()?.string()?.toApiErrorMessage()
                        ?: "Google sign-in failed. Please try again."
                }
                AuthResult.Error(msg)
            }
        } catch (e: Exception) {
            AuthResult.Error("Google sign-in error: ${e.localizedMessage}")
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

    private suspend fun saveLoginSession(
        body: LoginResponse,
        fallbackUserType: String,
        fallbackEmail: String,
    ): AuthResult.Success {
        val resolvedUserType = body.userType.ifBlank { fallbackUserType }
        val email = body.email.ifBlank { fallbackEmail }
        tokenManager.saveSession(
            accessToken = body.access,
            refreshToken = body.refresh,
            userType = resolvedUserType,
            role = body.role.orEmpty(),
            name = body.name.ifBlank { email },
            email = email,
            companyId = body.companyId,
        )
        return AuthResult.Success(body.copy(userType = resolvedUserType, email = email))
    }

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
