package com.neverno.neverq.customer.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.neverno.neverq.core.models.Banner
import com.neverno.neverq.core.models.Offer
import com.neverno.neverq.core.models.Offering
import com.neverno.neverq.core.models.Product
import com.neverno.neverq.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerHomeScreen(
    onCartClick: () -> Unit,
    onOrdersClick: () -> Unit,
    onProductClick: (Int) -> Unit,
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

    val filteredProducts = uiState.products.filter { product ->
        (selectedCategoryIds == null || product.categoryId in selectedCategoryIds) &&
            (!vegOnly || product.isVeg) &&
            (searchQuery.isBlank() || product.name.contains(searchQuery, ignoreCase = true))
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
                        BadgedBox(
                            badge = {
                                if (uiState.cartCount > 0) {
                                    Badge(containerColor = CfOrange) {
                                        Text("${uiState.cartCount}", color = Color.White, fontSize = 10.sp)
                                    }
                                }
                            },
                        ) {
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
            if (!uiState.isStoreOpen) {
                Row(
                    modifier = Modifier.fillMaxWidth().background(CfOrangeLight).padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(Icons.Default.AccessTime, null, tint = CfOrange, modifier = Modifier.size(16.dp))
                    Text(uiState.statusMessage, fontSize = 13.sp, color = CfOrange, fontWeight = FontWeight.Medium)
                }
            }

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

            CategoryChips(
                parentCategories = parentCategories,
                childCategories = childCategories,
                selectedParentCategoryId = selectedParentCategoryId,
                selectedChildCategoryId = selectedChildCategoryId,
                vegOnly = vegOnly,
                onParentSelected = { id ->
                    selectedParentCategoryId = id
                    selectedChildCategoryId = null
                },
                onChildSelected = { selectedChildCategoryId = it },
                onVegToggle = { vegOnly = !vegOnly },
            )

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CfBlue)
                }
            } else if (filteredProducts.isEmpty() && uiState.featuredProducts.isEmpty()) {
                EmptyMenuState()
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    if (uiState.cafes.size > 1) {
                        item {
                            CafeChoiceCard(
                                cafeName = uiState.cafes.firstOrNull { it.id == uiState.selectedCafeId }?.name
                                    ?: "Select cafe",
                            )
                        }
                    }
                    if (uiState.banners.isNotEmpty()) {
                        item { BannerStrip(uiState.banners) }
                    }
                    if (uiState.offerings.isNotEmpty()) {
                        item {
                            SectionTitle("Offerings", "Meal windows and service slots")
                            OfferingStrip(uiState.offerings)
                        }
                    }
                    if (uiState.offers.isNotEmpty()) {
                        item {
                            SectionTitle("Offers", "Active deals available now")
                            OfferStrip(uiState.offers)
                        }
                    }
                    if (uiState.featuredProducts.isNotEmpty()) {
                        item {
                            SectionTitle("Featured Products", "Popular picks for quick ordering")
                            FeaturedProductStrip(
                                products = uiState.featuredProducts,
                                onProductClick = onProductClick,
                                onAddToCart = { id -> viewModel.addToCart(id) },
                            )
                        }
                    }
                    item { SectionTitle("All Products", "${filteredProducts.size} available") }
                    items(filteredProducts, key = { it.id }) { product ->
                        CustomerProductCard(
                            product = product,
                            onClick = { onProductClick(product.id) },
                            onAddToCart = { viewModel.addToCart(product.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryChips(
    parentCategories: List<com.neverno.neverq.core.models.Category>,
    childCategories: List<com.neverno.neverq.core.models.Category>,
    selectedParentCategoryId: Int?,
    selectedChildCategoryId: Int?,
    vegOnly: Boolean,
    onParentSelected: (Int?) -> Unit,
    onChildSelected: (Int?) -> Unit,
    onVegToggle: () -> Unit,
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
    ) {
        item {
            FilterChip(
                selected = selectedParentCategoryId == null,
                onClick = { onParentSelected(null) },
                label = { Text("All", fontSize = 13.sp) },
                colors = chipColors(CfBlue),
            )
        }
        items(parentCategories, key = { it.id }) { category ->
            FilterChip(
                selected = selectedParentCategoryId == category.id,
                onClick = { onParentSelected(if (selectedParentCategoryId == category.id) null else category.id) },
                label = { Text(category.name, fontSize = 13.sp) },
                colors = chipColors(CfBlue),
            )
        }
        item {
            FilterChip(
                selected = vegOnly,
                onClick = onVegToggle,
                label = { Text("Veg", fontSize = 13.sp) },
                leadingIcon = if (vegOnly) {
                    { Icon(Icons.Default.Eco, null, modifier = Modifier.size(14.dp), tint = Color.White) }
                } else null,
                colors = chipColors(CfGreen),
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
                    onClick = { onChildSelected(null) },
                    label = { Text("All Submenu", fontSize = 13.sp) },
                    colors = chipColors(CfNavy),
                )
            }
            items(childCategories, key = { it.id }) { category ->
                FilterChip(
                    selected = selectedChildCategoryId == category.id,
                    onClick = { onChildSelected(if (selectedChildCategoryId == category.id) null else category.id) },
                    label = { Text(category.name, fontSize = 13.sp) },
                    colors = chipColors(CfNavy),
                )
            }
        }
    }
}

@Composable
private fun chipColors(selectedColor: Color) = FilterChipDefaults.filterChipColors(
    selectedContainerColor = selectedColor,
    selectedLabelColor = Color.White,
    selectedLeadingIconColor = Color.White,
)

@Composable
private fun EmptyMenuState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Default.SearchOff, null, tint = CfMuted, modifier = Modifier.size(48.dp))
            Text("No items found", fontWeight = FontWeight.SemiBold, color = CfNavy)
            Text("Try a different search or category", fontSize = 13.sp, color = CfMuted)
        }
    }
}

@Composable
private fun CafeChoiceCard(cafeName: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier.size(42.dp).clip(RoundedCornerShape(10.dp)).background(CfBlueLight),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Storefront, null, tint = CfBlue)
            }
            Column(Modifier.weight(1f)) {
                Text("Cafeteria", fontSize = 12.sp, color = CfMuted)
                Text(cafeName, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = CfNavy)
            }
        }
    }
}

@Composable
private fun BannerStrip(banners: List<Banner>) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(vertical = 4.dp)) {
        items(banners, key = { it.id }) { banner ->
            Card(
                modifier = Modifier.width(300.dp).height(136.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                if (banner.imageUrl != null) {
                    AsyncImage(
                        model = banner.imageUrl,
                        contentDescription = banner.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Box(Modifier.fillMaxSize().background(CfNavy), contentAlignment = Alignment.Center) {
                        Text(banner.name.ifBlank { "NeverQ" }, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String, subtitle: String) {
    Column(Modifier.padding(top = 8.dp, bottom = 2.dp)) {
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = CfNavy)
        Text(subtitle, fontSize = 12.sp, color = CfMuted)
    }
}

@Composable
private fun OfferingStrip(offerings: List<Offering>) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(vertical = 4.dp)) {
        items(offerings, key = { it.id }) { offering ->
            Card(
                modifier = Modifier.width(112.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(72.dp).clip(RoundedCornerShape(10.dp)).background(CfBlueLight),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (offering.imageUrl != null) {
                            AsyncImage(
                                model = offering.imageUrl,
                                contentDescription = offering.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                            )
                        } else {
                            Icon(Icons.Default.Schedule, null, tint = CfBlue)
                        }
                    }
                    Text(
                        offering.name,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CfNavy,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun OfferStrip(offers: List<Offer>) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(vertical = 4.dp)) {
        items(offers, key = { it.id }) { offer ->
            Card(
                modifier = Modifier.width(220.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier.size(46.dp).clip(RoundedCornerShape(10.dp)).background(CfOrangeLight),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.LocalOffer, null, tint = CfOrange)
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(offer.badgeLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CfOrange)
                        Text(offer.title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = CfNavy, maxLines = 2)
                    }
                }
            }
        }
    }
}

@Composable
private fun FeaturedProductStrip(
    products: List<Product>,
    onProductClick: (Int) -> Unit,
    onAddToCart: (Int) -> Unit,
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(vertical = 4.dp)) {
        items(products, key = { it.id }) { product ->
            Card(
                modifier = Modifier.width(188.dp).clickable { onProductClick(product.id) },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Column {
                    if (product.imageUrl != null) {
                        AsyncImage(
                            model = product.imageUrl,
                            contentDescription = product.name,
                            modifier = Modifier.fillMaxWidth().height(112.dp),
                            contentScale = ContentScale.Crop,
                        )
                    }
                    Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(product.categoryName.orEmpty(), fontSize = 11.sp, color = CfMuted, maxLines = 1)
                        Text(product.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CfNavy, maxLines = 2)
                        ProductPrice(product)
                        IconButton(
                            onClick = { onAddToCart(product.id) },
                            enabled = product.isAvailable,
                            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)).background(CfBlueLight),
                        ) {
                            Icon(Icons.Default.Add, "Add to cart", tint = CfBlue, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CustomerProductCard(product: Product, onClick: () -> Unit, onAddToCart: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            if (product.imageUrl != null) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.name,
                    modifier = Modifier.size(80.dp).clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop,
                )
                Spacer(modifier = Modifier.width(12.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(product.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = CfNavy)
                    if (product.isVeg) {
                        Spacer(Modifier.width(5.dp))
                        Icon(Icons.Default.Eco, null, tint = CfGreen, modifier = Modifier.size(14.dp))
                    }
                }
                product.description?.takeIf { it.isNotBlank() }?.let {
                    Text(it, fontSize = 12.sp, color = CfMuted, maxLines = 2)
                }
                Spacer(modifier = Modifier.height(6.dp))
                ProductPrice(product)
                if (product.hasOffer && product.offerTitle.isNotBlank()) {
                    Text(product.offerTitle, fontSize = 11.sp, color = CfOrange, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(Modifier.width(8.dp))
            if (product.isAvailable) {
                IconButton(
                    onClick = onAddToCart,
                    modifier = Modifier.size(38.dp).clip(RoundedCornerShape(8.dp)).background(CfBlueLight),
                ) {
                    Icon(Icons.Default.Add, "Add to cart", tint = CfBlue, modifier = Modifier.size(20.dp))
                }
            } else {
                Box(
                    modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(CfBorder).padding(horizontal = 10.dp, vertical = 6.dp),
                ) {
                    Text("Sold Out", fontSize = 11.sp, color = CfMuted, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
private fun ProductPrice(product: Product) {
    val displayPrice = product.displayPrice.ifBlank { product.price }
    Column {
        Text("Rs $displayPrice", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = CfBlue)
        product.discountedPrice?.let {
            Text("Offer Rs $it", fontSize = 11.sp, color = CfGreen, fontWeight = FontWeight.SemiBold)
        }
    }
}
