package com.neverno.neverq.customer.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neverno.neverq.auth.AuthRepository
import com.neverno.neverq.core.models.AddToCartRequest
import com.neverno.neverq.core.models.Banner
import com.neverno.neverq.core.models.Cafe
import com.neverno.neverq.core.models.Category
import com.neverno.neverq.core.models.Offer
import com.neverno.neverq.core.models.Offering
import com.neverno.neverq.core.models.OrderListItem
import com.neverno.neverq.core.models.Product
import com.neverno.neverq.core.network.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CustomerMenuUiState(
    val categories: List<Category> = emptyList(),
    val products: List<Product> = emptyList(),
    val featuredProducts: List<Product> = emptyList(),
    val banners: List<Banner> = emptyList(),
    val offerings: List<Offering> = emptyList(),
    val offers: List<Offer> = emptyList(),
    val recentOrders: List<OrderListItem> = emptyList(),
    val cafes: List<Cafe> = emptyList(),
    val selectedCafeId: Int? = null,
    val isStoreOpen: Boolean = true,
    val statusMessage: String = "",
    val storeName: String = "Menu",
    val orderWindowLabel: String? = null,
    val cartCount: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val navigateTo: String? = null,
)

@HiltViewModel
class CustomerMenuViewModel @Inject constructor(
    private val api: ApiService,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CustomerMenuUiState())
    val uiState: StateFlow<CustomerMenuUiState> = _uiState

    init {
        loadMenu()
        loadCart()
    }

    fun loadMenu() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val response = api.getMenu()
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    _uiState.value = _uiState.value.copy(
                        categories = body.categories,
                        products = body.products,
                        featuredProducts = body.featuredProducts,
                        banners = body.banners,
                        offerings = body.offerings,
                        offers = body.offers,
                        recentOrders = body.recentOrders,
                        cafes = body.cafes,
                        selectedCafeId = body.selectedCafeId,
                        isStoreOpen = body.isStoreOpen,
                        statusMessage = body.orderingStatusMessage,
                        storeName = body.storeName.ifBlank { _uiState.value.storeName },
                        orderWindowLabel = body.orderWindowLabel,
                        isLoading = false,
                    )
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Failed to load menu.")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.localizedMessage)
            }
        }
    }

    private fun loadCart() {
        viewModelScope.launch {
            try {
                val response = api.getCart()
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(cartCount = response.body()?.itemCount ?: 0)
                }
            } catch (_: Exception) {}
        }
    }

    fun addToCart(productId: Int) {
        viewModelScope.launch {
            try {
                api.addToCart(AddToCartRequest(productId, 1))
                _uiState.value = _uiState.value.copy(cartCount = _uiState.value.cartCount + 1)
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
