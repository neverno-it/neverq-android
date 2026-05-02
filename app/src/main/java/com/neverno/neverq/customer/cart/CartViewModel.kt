package com.neverno.neverq.customer.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neverno.neverq.core.models.*
import com.neverno.neverq.core.network.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CartUiState(
    val items: List<CartItem> = emptyList(),
    val subtotal: String = "0",
    val isLoading: Boolean = false,
    val error: String? = null,
    val placedOrderId: Int? = null,
)

@HiltViewModel
class CartViewModel @Inject constructor(private val api: ApiService) : ViewModel() {

    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState

    init { loadCart() }

    fun loadCart() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val response = api.getCart()
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    _uiState.value = CartUiState(
                        items = body.items,
                        subtotal = body.subtotal,
                    )
                }
            } catch (e: Exception) {
                _uiState.value = CartUiState(error = e.localizedMessage)
            }
        }
    }

    fun updateQty(itemId: Int, newQty: Int) {
        val item = _uiState.value.items.find { it.id == itemId } ?: return
        viewModelScope.launch {
            try {
                api.addToCart(AddToCartRequest(item.product.id, newQty))
                loadCart()
            } catch (_: Exception) {}
        }
    }

    fun removeItem(itemId: Int) {
        viewModelScope.launch {
            try {
                api.removeFromCart(RemoveCartItemRequest(itemId))
                loadCart()
            } catch (_: Exception) {}
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            try {
                api.clearCart()
                _uiState.value = CartUiState()
            } catch (_: Exception) {}
        }
    }

    fun checkout() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val response = api.checkout(CheckoutRequest(paymentMode = "cod"))
                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        placedOrderId = response.body()!!.orderId,
                    )
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Checkout failed.")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.localizedMessage)
            }
        }
    }
}
