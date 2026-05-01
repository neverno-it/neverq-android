package com.neverno.neverq.pos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neverno.neverq.core.db.daos.PosProductDao
import com.neverno.neverq.core.db.entities.CachedPosProduct
import com.neverno.neverq.core.models.*
import com.neverno.neverq.core.network.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PosCartItem(val name: String, val price: Double, var qty: Int = 1) {
    val total get() = price * qty
}

data class PosUiState(
    val products: List<PosProduct> = emptyList(),
    val cart: List<PosCartItem> = emptyList(),
    val isLoading: Boolean = false,
    val isOffline: Boolean = false,
    val lastOrder: PosOrder? = null,
    val error: String? = null,
)

@HiltViewModel
class PosViewModel @Inject constructor(
    private val api: ApiService,
    private val dao: PosProductDao,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PosUiState())
    val uiState: StateFlow<PosUiState> = _uiState

    init { loadProducts() }

    fun loadProducts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val response = api.getPosProducts()
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    val all = body.posProducts + body.menuProducts
                    dao.clearAll()
                    dao.insertAll(all.map { CachedPosProduct(it.id, it.name, it.price, "mixed") })
                    _uiState.value = _uiState.value.copy(
                        products = all, isLoading = false, isOffline = false,
                    )
                } else {
                    loadFromCache()
                }
            } catch (e: Exception) {
                loadFromCache()
            }
        }
    }

    private suspend fun loadFromCache() {
        val cached = dao.getAll().map { PosProduct(it.id, it.name, it.price) }
        _uiState.value = _uiState.value.copy(products = cached, isLoading = false, isOffline = true)
    }

    fun addToCart(product: PosProduct) {
        val price = product.price.toDoubleOrNull() ?: return
        val cart = _uiState.value.cart.toMutableList()
        val existing = cart.indexOfFirst { it.name == product.name }
        if (existing >= 0) {
            cart[existing] = cart[existing].copy(qty = cart[existing].qty + 1)
        } else {
            cart.add(PosCartItem(product.name, price))
        }
        _uiState.value = _uiState.value.copy(cart = cart)
    }

    fun removeFromCart(name: String) {
        val cart = _uiState.value.cart.toMutableList()
        val i = cart.indexOfFirst { it.name == name }
        if (i >= 0) {
            if (cart[i].qty > 1) cart[i] = cart[i].copy(qty = cart[i].qty - 1)
            else cart.removeAt(i)
        }
        _uiState.value = _uiState.value.copy(cart = cart)
    }

    fun clearCart() {
        _uiState.value = _uiState.value.copy(cart = emptyList())
    }

    fun placeOrder(paymentType: Int, customerName: String = "Walk-in Customer") {
        val cart = _uiState.value.cart
        if (cart.isEmpty()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val request = PosOrderRequest(
                    customerName = customerName,
                    paymentType = paymentType,
                    items = cart.map {
                        PosOrderItemInput(
                            productName = it.name,
                            price = String.format("%.2f", it.price),
                            qty = it.qty,
                        )
                    }
                )
                val response = api.createPosOrder(request)
                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = _uiState.value.copy(
                        lastOrder = response.body(),
                        cart = emptyList(),
                        isLoading = false,
                    )
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Order failed.")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.localizedMessage)
            }
        }
    }

    fun dismissReceipt() {
        _uiState.value = _uiState.value.copy(lastOrder = null)
    }
}
