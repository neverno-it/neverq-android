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
    val phone: String = "",
    val companyId: Int? = null,
    val companyName: String? = null,
    val walletBalance: String = "0.00",
    val royaltyPoints: Int = 0,
    val mealBenefit: String = "",
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
                    phone = _uiState.value.phone,
                    companyId = companyId,
                    companyName = _uiState.value.companyName,
                    walletBalance = _uiState.value.walletBalance,
                    royaltyPoints = _uiState.value.royaltyPoints,
                    mealBenefit = _uiState.value.mealBenefit,
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
                    val body = response.body()!!
                    _uiState.value = _uiState.value.copy(
                        name = body.name,
                        email = body.email,
                        phone = body.phone.orEmpty(),
                        companyName = body.companyName,
                        walletBalance = body.walletBalance,
                        royaltyPoints = body.royaltyPoints,
                        mealBenefit = body.mealBenefit,
                    )
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
