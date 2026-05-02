package com.neverno.neverq.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neverno.neverq.auth.AuthRepository
import com.neverno.neverq.core.data.TokenManager
import com.neverno.neverq.core.network.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val name: String = "",
    val email: String = "",
    val companyId: Int? = null,
    val companyName: String? = null,
    val navigateTo: String? = null,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val tokenManager: TokenManager,
    private val api: ApiService,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState

    init {
        observeStoredProfile()
        loadProfile()
    }

    private fun observeStoredProfile() {
        viewModelScope.launch {
            combine(
                tokenManager.userName,
                tokenManager.userEmail,
                tokenManager.companyId,
            ) { name, email, companyId ->
                ProfileUiState(
                    name = name.orEmpty(),
                    email = email.orEmpty(),
                    companyId = companyId,
                    companyName = _uiState.value.companyName,
                    navigateTo = _uiState.value.navigateTo,
                )
            }.collect { profile ->
                _uiState.value = profile
            }
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            try {
                val response = api.getProfile()
                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = _uiState.value.copy(companyName = response.body()!!.companyName)
                }
            } catch (_: Exception) {}
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.value = _uiState.value.copy(navigateTo = "login")
        }
    }
}
