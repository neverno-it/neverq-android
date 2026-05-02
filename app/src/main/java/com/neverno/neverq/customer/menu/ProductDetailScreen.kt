package com.neverno.neverq.customer.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.neverno.neverq.core.models.AddToCartRequest
import com.neverno.neverq.core.models.Product
import com.neverno.neverq.core.network.ApiService
import com.neverno.neverq.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProductDetailUiState(
    val product: Product? = null,
    val similarProducts: List<Product> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val addedMessage: String? = null,
)

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val api: ApiService,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val productId: Int = checkNotNull(savedStateHandle.get<Int>("productId"))
    private val _uiState = MutableStateFlow(ProductDetailUiState(isLoading = true))
    val uiState: StateFlow<ProductDetailUiState> = _uiState

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val response = api.getProductDetail(productId)
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    _uiState.value = ProductDetailUiState(
                        product = body.product,
                        similarProducts = body.similarProducts,
                        isLoading = false,
                    )
                } else {
                    _uiState.value = ProductDetailUiState(isLoading = false, error = "Product not found.")
                }
            } catch (e: Exception) {
                _uiState.value = ProductDetailUiState(isLoading = false, error = e.localizedMessage)
            }
        }
    }

    fun addToCart(productId: Int) {
        viewModelScope.launch {
            try {
                val response = api.addToCart(AddToCartRequest(productId, 1))
                _uiState.value = _uiState.value.copy(
                    addedMessage = if (response.isSuccessful) "Added to cart" else "Could not add item",
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(addedMessage = e.localizedMessage)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    onBack: () -> Unit,
    onProductClick: (Int) -> Unit,
    viewModel: ProductDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.addedMessage) {
        uiState.addedMessage?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Product", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CfNavy),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = CfSurface,
    ) { padding ->
        when {
            uiState.isLoading -> Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = CfBlue)
            }
            uiState.error != null -> Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(uiState.error.orEmpty(), color = CfRed)
            }
            uiState.product != null -> ProductDetailContent(
                product = uiState.product!!,
                similarProducts = uiState.similarProducts,
                onAddToCart = { id -> viewModel.addToCart(id) },
                onProductClick = onProductClick,
                modifier = Modifier.padding(padding),
            )
        }
    }
}

@Composable
private fun ProductDetailContent(
    product: Product,
    similarProducts: List<Product>,
    onAddToCart: (Int) -> Unit,
    onProductClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Column {
                    if (product.imageUrl != null) {
                        AsyncImage(
                            model = product.imageUrl,
                            contentDescription = product.name,
                            modifier = Modifier.fillMaxWidth().height(220.dp),
                            contentScale = ContentScale.Crop,
                        )
                    }
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(product.name, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = CfNavy, modifier = Modifier.weight(1f))
                            if (product.isVeg) Icon(Icons.Default.Eco, null, tint = CfGreen)
                        }
                        ProductDetailPrice(product)
                        if (product.hasOffer && product.offerTitle.isNotBlank()) {
                            Row(
                                modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(CfOrangeLight).padding(10.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(Icons.Default.LocalOffer, null, tint = CfOrange, modifier = Modifier.size(18.dp))
                                Text(product.offerTitle, color = CfOrange, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            }
                        }
                        product.description?.takeIf { it.isNotBlank() }?.let {
                            Text(it, color = CfText, fontSize = 14.sp, lineHeight = 20.sp)
                        }
                        if (product.calories != null || product.foodLabel.isNotBlank()) {
                            Text(
                                listOfNotNull(
                                    product.foodLabel.takeIf { it.isNotBlank() },
                                    product.calories?.let { "$it kcal" },
                                ).joinToString(" | "),
                                color = CfMuted,
                                fontSize = 12.sp,
                            )
                        }
                        Button(
                            onClick = { onAddToCart(product.id) },
                            enabled = product.isAvailable,
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CfBlue),
                        ) {
                            Text(if (product.isAvailable) "Add to Cart" else "Sold Out", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        if (similarProducts.isNotEmpty()) {
            item {
                Text("Similar Products", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = CfNavy)
            }
            items(similarProducts, key = { it.id }) { item ->
                CustomerProductCard(
                    product = item,
                    onClick = { onProductClick(item.id) },
                    onAddToCart = { onAddToCart(item.id) },
                )
            }
        }
    }
}

@Composable
private fun ProductDetailPrice(product: Product) {
    val displayPrice = product.displayPrice.ifBlank { product.price }
    Column {
        Text("Rs $displayPrice", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = CfBlue)
        product.discountedPrice?.let {
            Text("Offer Rs $it", fontSize = 13.sp, color = CfGreen, fontWeight = FontWeight.SemiBold)
        }
    }
}
