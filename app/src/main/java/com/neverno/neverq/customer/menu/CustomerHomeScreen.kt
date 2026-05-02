package com.neverno.neverq.customer.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import coil.compose.AsyncImage
import com.neverno.neverq.core.models.Product
import com.neverno.neverq.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerHomeScreen(
    onCartClick: () -> Unit,
    onOrdersClick: () -> Unit,
    onLogout: () -> Unit,
    viewModel: CustomerMenuViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedParentCategoryId by remember { mutableStateOf<Int?>(null) }
    var selectedChildCategoryId by remember { mutableStateOf<Int?>(null) }
    var vegOnly by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.navigateTo) {
        if (uiState.navigateTo == "login") onLogout()
    }

    val rootCategories = uiState.categories.filter { it.parentId == null }
    val parentCategories = rootCategories.ifEmpty { uiState.categories }
    val childCategories = selectedParentCategoryId?.let { parentId ->
        uiState.categories.filter { it.parentId == parentId }
    }.orEmpty()
    val selectedCategoryIds = when {
        selectedChildCategoryId != null -> setOf(selectedChildCategoryId!!)
        selectedParentCategoryId != null -> buildSet {
            add(selectedParentCategoryId!!)
            addAll(childCategories.map { it.id })
        }
        else -> null
    }

    val filteredProducts = uiState.products.filter { p ->
        (selectedCategoryIds == null || p.categoryId in selectedCategoryIds) &&
        (!vegOnly || p.isVeg) &&
        (searchQuery.isBlank() || p.name.contains(searchQuery, ignoreCase = true))
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Sign Out", fontWeight = FontWeight.SemiBold) },
            text = { Text("Are you sure you want to sign out?") },
            confirmButton = {
                Button(
                    onClick = { showLogoutDialog = false; viewModel.logout() },
                    colors = ButtonDefaults.buttonColors(containerColor = CfRed),
                ) { Text("Sign Out") }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel") }
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        uiState.storeName.ifBlank { "Menu" },
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        fontSize = 18.sp,
                    )
                },
                actions = {
                    IconButton(onClick = onOrdersClick) {
                        Icon(Icons.Default.History, "My Orders", tint = Color.White)
                    }
                    IconButton(onClick = onCartClick) {
                        BadgedBox(badge = {
                            if (uiState.cartCount > 0) {
                                Badge(containerColor = CfOrange) {
                                    Text("${uiState.cartCount}", color = Color.White, fontSize = 10.sp)
                                }
                            }
                        }) {
                            Icon(Icons.Default.ShoppingCart, "Cart", tint = Color.White)
                        }
                    }
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(Icons.Default.Logout, "Sign Out", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CfNavy),
            )
        },
        containerColor = CfSurface,
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            // Store closed banner
            if (!uiState.isStoreOpen) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CfOrangeLight)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(Icons.Default.AccessTime, null, tint = CfOrange, modifier = Modifier.size(16.dp))
                    Text(uiState.statusMessage, fontSize = 13.sp, color = CfOrange, fontWeight = FontWeight.Medium)
                }
            }

            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search menu...", color = CfMuted, fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = CfMuted) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CfBlue,
                    unfocusedBorderColor = CfBorder,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                ),
            )

            // Category chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            ) {
                item {
                    FilterChip(
                        selected = selectedParentCategoryId == null,
                        onClick = {
                            selectedParentCategoryId = null
                            selectedChildCategoryId = null
                        },
                        label = { Text("All", fontSize = 13.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CfBlue,
                            selectedLabelColor = Color.White,
                        ),
                    )
                }
                items(parentCategories, key = { it.id }) { cat ->
                    FilterChip(
                        selected = selectedParentCategoryId == cat.id,
                        onClick = {
                            selectedParentCategoryId = if (selectedParentCategoryId == cat.id) null else cat.id
                            selectedChildCategoryId = null
                        },
                        label = { Text(cat.name, fontSize = 13.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CfBlue,
                            selectedLabelColor = Color.White,
                        ),
                    )
                }
            }

            if (childCategories.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                ) {
                    item {
                        FilterChip(
                            selected = selectedChildCategoryId == null,
                            onClick = { selectedChildCategoryId = null },
                            label = { Text("All Submenu", fontSize = 13.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CfNavy,
                                selectedLabelColor = Color.White,
                            ),
                        )
                    }
                    items(childCategories, key = { it.id }) { cat ->
                        FilterChip(
                            selected = selectedChildCategoryId == cat.id,
                            onClick = { selectedChildCategoryId = if (selectedChildCategoryId == cat.id) null else cat.id },
                            label = { Text(cat.name, fontSize = 13.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CfNavy,
                                selectedLabelColor = Color.White,
                            ),
                        )
                    }
                }
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            ) {
                item {
                    FilterChip(
                        selected = vegOnly,
                        onClick = { vegOnly = !vegOnly },
                        label = { Text("Veg Only", fontSize = 13.sp) },
                        leadingIcon = if (vegOnly) {
                            { Icon(Icons.Default.Eco, null, modifier = Modifier.size(14.dp), tint = Color.White) }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CfGreen,
                            selectedLabelColor = Color.White,
                        ),
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CfBlue)
                }
            } else if (filteredProducts.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.SearchOff, null, tint = CfMuted, modifier = Modifier.size(48.dp))
                        Text("No items found", fontWeight = FontWeight.SemiBold, color = CfNavy)
                        Text("Try a different search or category", fontSize = 13.sp, color = CfMuted)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(filteredProducts, key = { it.id }) { product ->
                        CustomerProductCard(
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
fun CustomerProductCard(product: Product, onAddToCart: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (product.imageUrl != null) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.name,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop,
                )
                Spacer(modifier = Modifier.width(12.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        product.name,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = CfNavy,
                    )
                    if (product.isVeg) {
                        Spacer(Modifier.width(5.dp))
                        Icon(Icons.Default.Eco, null, tint = CfGreen, modifier = Modifier.size(14.dp))
                    }
                }
                product.description?.let {
                    Text(it, fontSize = 12.sp, color = CfMuted, maxLines = 2)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text("₹${product.price}", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = CfBlue)
            }
            Spacer(Modifier.width(8.dp))
            if (product.isAvailable) {
                IconButton(
                    onClick = onAddToCart,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CfBlueLight),
                ) {
                    Icon(Icons.Default.Add, "Add to cart", tint = CfBlue, modifier = Modifier.size(20.dp))
                }
            } else {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(CfBorder)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                ) {
                    Text("Sold Out", fontSize = 11.sp, color = CfMuted, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
