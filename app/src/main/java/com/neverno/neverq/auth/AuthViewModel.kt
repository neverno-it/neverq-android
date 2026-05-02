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

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = LoginUiState(error = "Email and password are required.")
            return
        }
        viewModelScope.launch {
            _uiState.value = LoginUiState(isLoading = true)
            when (val result = repo.login(email, password)) {
                is AuthResult.Success -> {
                    val route = routeForRole(result.response.userType, result.response.role)
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

    private fun routeForRole(userType: String, role: String): String = when {
        userType == "customer" -> "customer"
        role == "cafeman" -> "kitchen"
        role == "pos" -> "pos"
        role in listOf("admin", "superadmin") -> "admin"
        else -> "admin"
    }
}
