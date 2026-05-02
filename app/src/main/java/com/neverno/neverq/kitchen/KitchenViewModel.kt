package com.neverno.neverq.kitchen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neverno.neverq.auth.AuthRepository
import com.neverno.neverq.core.db.daos.KitchenOrderDao
import com.neverno.neverq.core.db.entities.CachedKitchenOrder
import com.neverno.neverq.core.models.KitchenOrder
import com.neverno.neverq.core.models.UpdateStatusRequest
import com.neverno.neverq.core.network.ApiService
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class KitchenUiState(
    val orders: List<KitchenOrder> = emptyList(),
    val isLoading: Boolean = false,
    val isOffline: Boolean = false,
    val error: String? = null,
    val navigateTo: String? = null,
)

@HiltViewModel
class KitchenViewModel @Inject constructor(
    private val api: ApiService,
    private val dao: KitchenOrderDao,
    private val moshi: Moshi,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(KitchenUiState())
    val uiState: StateFlow<KitchenUiState> = _uiState

    private val itemsType = Types.newParameterizedType(
        List::class.java,
        com.neverno.neverq.core.models.OrderItemDetail::class.java
    )
    private val itemsAdapter by lazy { moshi.adapter<List<com.neverno.neverq.core.models.OrderItemDetail>>(itemsType) }

    init {
        observeCache()
        refresh()
    }

    private fun observeCache() {
        viewModelScope.launch {
            dao.observeAll().collectLatest { cached ->
                if (_uiState.value.isOffline) {
                    _uiState.value = _uiState.value.copy(orders = cached.map { it.toDomain() })
                }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val response = api.getKitchenOrders()
                if (response.isSuccessful && response.body() != null) {
                    val orders = response.body()!!
                    dao.insertAll(orders.map { it.toCached() })
                    _uiState.value = KitchenUiState(orders = orders, isOffline = false)
                } else {
                    fallbackToCache()
                }
            } catch (e: Exception) {
                fallbackToCache()
            }
        }
    }

    private suspend fun fallbackToCache() {
        val cached = dao.getAll().map { it.toDomain() }
        _uiState.value = KitchenUiState(
            orders = cached,
            isOffline = true,
            isLoading = false,
            error = if (cached.isEmpty()) "No connection and no cached data." else null,
        )
    }

    fun updateStatus(orderId: Int, newStatus: Int) {
        viewModelScope.launch {
            try {
                val response = api.updateKitchenOrderStatus(orderId, UpdateStatusRequest(newStatus))
                if (response.isSuccessful) {
                    val updated = response.body()!!
                    dao.updateStatus(orderId, updated.orderStatus, updated.statusLabel)
                    val updatedOrders = _uiState.value.orders.map { o ->
                        if (o.id == orderId) o.copy(
                            orderStatus = updated.orderStatus,
                            statusLabel = updated.statusLabel,
                        ) else o
                    }.filter { it.orderStatus !in listOf(5, 6) }
                    _uiState.value = _uiState.value.copy(orders = updatedOrders)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Failed to update: ${e.localizedMessage}")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.value = _uiState.value.copy(navigateTo = "login")
        }
    }

    private fun KitchenOrder.toCached() = CachedKitchenOrder(
        id = id,
        orderNumber = orderNumber,
        orderStatus = orderStatus,
        statusLabel = statusLabel,
        totalAmount = totalAmount,
        paymentMode = paymentMode,
        createdAt = createdAt,
        displayCustomerName = displayCustomerName,
        displayCustomerPhone = displayCustomerPhone,
        itemsJson = itemsAdapter.toJson(items),
    )

    private fun CachedKitchenOrder.toDomain() = KitchenOrder(
        id = id,
        orderNumber = orderNumber,
        orderStatus = orderStatus,
        statusLabel = statusLabel,
        totalAmount = totalAmount,
        paymentMode = paymentMode,
        createdAt = createdAt,
        displayCustomerName = displayCustomerName,
        displayCustomerPhone = displayCustomerPhone,
        items = itemsAdapter.fromJson(itemsJson) ?: emptyList(),
    )
}
