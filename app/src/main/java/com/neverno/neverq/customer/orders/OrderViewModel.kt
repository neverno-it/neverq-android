package com.neverno.neverq.customer.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neverno.neverq.core.models.OrderDetail
import com.neverno.neverq.core.models.OrderListItem
import com.neverno.neverq.core.network.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OrderListUiState(
    val orders: List<OrderListItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

data class OrderDetailUiState(
    val order: OrderDetail? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class OrderViewModel @Inject constructor(private val api: ApiService) : ViewModel() {

    private val _uiState = MutableStateFlow(OrderListUiState())
    val uiState: StateFlow<OrderListUiState> = _uiState

    private val _detailState = MutableStateFlow(OrderDetailUiState())
    val detailState: StateFlow<OrderDetailUiState> = _detailState

    init { loadOrders() }

    fun loadOrders() {
        viewModelScope.launch {
            _uiState.value = OrderListUiState(isLoading = true)
            try {
                val response = api.getOrders()
                _uiState.value = if (response.isSuccessful)
                    OrderListUiState(orders = response.body() ?: emptyList())
                else
                    OrderListUiState(error = "Failed to load orders.")
            } catch (e: Exception) {
                _uiState.value = OrderListUiState(error = e.localizedMessage)
            }
        }
    }

    fun loadOrderDetail(orderId: Int) {
        viewModelScope.launch {
            _detailState.value = OrderDetailUiState(isLoading = true)
            try {
                val response = api.getOrderDetail(orderId)
                _detailState.value = if (response.isSuccessful)
                    OrderDetailUiState(order = response.body())
                else
                    OrderDetailUiState(error = "Order not found.")
            } catch (e: Exception) {
                _detailState.value = OrderDetailUiState(error = e.localizedMessage)
            }
        }
    }
}
