package com.neverno.neverq.customer.menu

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.neverno.neverq.core.models.Product

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerHomeScreen(
    onCartClick: () -> Unit,
    onOrdersClick: () -> Unit,
    viewModel: CustomerMenuViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    var vegOnly by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredProducts = uiState.products.filter { p ->
        (selectedCategoryId == null || p.categoryId == selectedCategoryId) &&
        (!vegOnly || p.isVeg) &&
        (searchQuery.isBlank() || p.name.contains(searchQuery, ignoreCase = true))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.storeName) },
                actions = {
                    IconButton(onClick = onOrdersClick) { Icon(Icons.Default.History, "Orders") }
                    IconButton(onClick = onCartClick) {
                        BadgedBox(badge = {
                            if (uiState.cartCount > 0) Badge { Text("${uiState.cartCount}") }
                        }) {
                            Icon(Icons.Default.ShoppingCart, "Cart")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            // Store status banner
            if (!uiState.isStoreOpen) {
                Surface(color = MaterialTheme.colorScheme.errorContainer) {
                    Text(
                        text = uiState.statusMessage,
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search menu...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true,
            )

            // Category chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
            ) {
                item {
                    FilterChip(
                        selected = selectedCategoryId == null,
                        onClick = { selectedCategoryId = null },
                        label = { Text("All") },
                    )
                }
                items(uiState.categories) { cat ->
                    FilterChip(
                        selected = selectedCategoryId == cat.id,
                        onClick = { selectedCategoryId = if (selectedCategoryId == cat.id) null else cat.id },
                        label = { Text(cat.name) },
                    )
                }
                item {
                    FilterChip(
                        selected = vegOnly,
                        onClick = { vegOnly = !vegOnly },
                        label = { Text("Veg Only") },
                        leadingIcon = if (vegOnly) {
                            { Icon(Icons.Default.Eco, null, modifier = Modifier.size(16.dp)) }
                        } else null,
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(filteredProducts, key = { it.id }) { product ->
                        ProductCard(
                            product = product,
                            onAddToCart = { viewModel.addToCart(product.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProductCard(product: Product, onAddToCart: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            if (product.imageUrl != null) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.name,
                    modifier = Modifier.size(80.dp),
                    contentScale = ContentScale.Crop,
                )
                Spacer(modifier = Modifier.width(12.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(product.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    if (product.isVeg) {
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.Default.Eco, null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(14.dp))
                    }
                }
                product.description?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("₹${product.price}", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            }
            IconButton(
                onClick = onAddToCart,
                enabled = product.isAvailable,
            ) {
                Icon(Icons.Default.AddCircle, contentDescription = "Add to cart", tint = if (product.isAvailable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
            }
        }
    }
}
