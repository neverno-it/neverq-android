package com.neverno.neverq.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val navigateTo: String? = null,
)

@HiltViewModel
class AuthViewModel @Inject constructor(private val repo: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    fun login(email: String, password: String, userType: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = LoginUiState(error = "Email and password are required.")
            return
        }
        viewModelScope.launch {
            _uiState.value = LoginUiState(isLoading = true)
            when (val result = repo.login(email, password, userType)) {
                is AuthResult.Success -> {
                    val route = routeForRole(
                        userType = result.response.userType.ifBlank { userType },
                        role = result.response.role,
                    )
                    _uiState.value = LoginUiState(navigateTo = route)
                }
                is AuthResult.Error -> {
                    _uiState.value = LoginUiState(error = result.message)
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private fun routeForRole(userType: String?, role: String?): String {
        val normalizedUserType = userType.orEmpty().trim().lowercase()
        val normalizedRole = role.orEmpty().trim().lowercase()

        return when {
            normalizedUserType == "customer" || normalizedRole == "customer" -> "customer"
            normalizedRole in listOf("cafeman", "cafe_manager", "kitchen") -> "kitchen"
            normalizedRole in listOf("pos", "cashier") -> "pos"
            normalizedRole in listOf("admin", "superadmin", "owner", "manager") -> "admin"
            normalizedUserType in listOf("staff", "admin") -> "admin"
            else -> "admin"
        }
    }
}
