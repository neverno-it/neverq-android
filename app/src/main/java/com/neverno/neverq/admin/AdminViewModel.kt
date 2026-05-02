package com.neverno.neverq.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neverno.neverq.auth.AuthRepository
import com.neverno.neverq.core.models.*
import com.neverno.neverq.core.network.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminUiState(
    val stats: DashboardStats? = null,
    val weeklyRevenue: List<WeeklyRevenue> = emptyList(),
    val orders: List<OrderListItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val navigateTo: String? = null,
)

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val api: ApiService,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState

    init { loadDashboard() }

    fun loadDashboard() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val response = api.getDashboard()
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    _uiState.value = AdminUiState(
                        stats = body.stats,
                        weeklyRevenue = body.weeklyRevenue,
                        isLoading = false,
                    )
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Failed to load dashboard.")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.localizedMessage)
            }
        }
    }

    fun loadOrders(status: Int? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val response = api.getAdminOrders(status = status)
                _uiState.value = _uiState.value.copy(
                    orders = response.body() ?: emptyList(),
                    isLoading = false,
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.localizedMessage)
            }
        }
    }

    fun updateOrderStatus(orderId: Int, newStatus: Int) {
        viewModelScope.launch {
            try {
                api.updateAdminOrderStatus(orderId, UpdateStatusRequest(newStatus))
                loadOrders()
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
