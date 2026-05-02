package com.neverno.neverq.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.neverno.neverq.core.models.Category
import com.neverno.neverq.core.models.Product
import com.neverno.neverq.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCatalogScreen(viewModel: AdminViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(selectedCategoryId) { viewModel.loadCatalog(selectedCategoryId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Catalog", fontWeight = FontWeight.SemiBold, color = Color.White, fontSize = 18.sp) },
                actions = {
                    IconButton(onClick = { viewModel.loadCatalog(selectedCategoryId) }) {
                        Icon(Icons.Default.Refresh, "Refresh", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CfNavy),
            )
        },
        containerColor = CfSurface,
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            ScrollableTabRow(
                selectedTabIndex = uiState.categories.indexOfFirst { it.id == selectedCategoryId }.let { if (it < 0) 0 else it + 1 },
                containerColor = Color.White,
                contentColor = CfBlue,
                edgePadding = 8.dp,
            ) {
                Tab(
                    selected = selectedCategoryId == null,
                    onClick = { selectedCategoryId = null },
                    text = { Text("All", fontSize = 13.sp) },
                    selectedContentColor = CfBlue,
                    unselectedContentColor = CfMuted,
                )
                uiState.categories.forEach { category ->
                    Tab(
                        selected = selectedCategoryId == category.id,
                        onClick = { selectedCategoryId = category.id },
                        text = { Text(category.name, fontSize = 13.sp) },
                        selectedContentColor = CfBlue,
                        unselectedContentColor = CfMuted,
                    )
                }
            }

            Box(Modifier.fillMaxSize()) {
                when {
                    uiState.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center), color = CfBlue)
                    uiState.error != null -> Text(uiState.error!!, Modifier.align(Alignment.Center), color = CfRed)
                    uiState.products.isEmpty() && uiState.categories.isEmpty() -> EmptyAdminState("No catalog items found", Modifier.align(Alignment.Center))
                    else -> LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        item { CatalogSummaryCard(uiState.products.size, uiState.categories.size) }
                        if (selectedCategoryId == null && uiState.categories.isNotEmpty()) {
                            item {
                                Text("Categories", fontSize = 13.sp, color = CfMuted, fontWeight = FontWeight.SemiBold)
                            }
                            items(uiState.categories, key = { it.id }) { category ->
                                CategoryCard(category)
                            }
                        }
                        item {
                            Text("Products", fontSize = 13.sp, color = CfMuted, fontWeight = FontWeight.SemiBold)
                        }
                        items(uiState.products, key = { it.id }) { product ->
                            ProductCard(product)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CatalogSummaryCard(productCount: Int, categoryCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SummaryPill("Products", productCount.toString(), Icons.Default.Inventory, CfBlue, CfBlueLight, Modifier.weight(1f))
            SummaryPill("Categories", categoryCount.toString(), Icons.Default.Category, CfPurple, CfPurpleLight, Modifier.weight(1f))
        }
    }
}

@Composable
private fun CategoryCard(category: Category) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier.size(42.dp).clip(RoundedCornerShape(10.dp)).background(CfBlueLight),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Category, null, tint = CfBlue)
            }
            Column(Modifier.weight(1f)) {
                Text(category.name, color = CfNavy, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text("Slug: ${category.slug}", color = CfMuted, fontSize = 12.sp)
            }
            Text("Sort ${category.sortOrder}", color = CfMuted, fontSize = 12.sp)
        }
    }
}

@Composable
private fun ProductCard(product: Product) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier.size(42.dp).clip(RoundedCornerShape(10.dp)).background(if (product.isAvailable) CfGreenLight else CfRedLight),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Inventory, null, tint = if (product.isAvailable) CfGreen else CfRed)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(product.name, color = CfNavy, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    if (product.isVeg) {
                        Spacer(Modifier.width(5.dp))
                        Icon(Icons.Default.Eco, null, tint = CfGreen, modifier = Modifier.size(14.dp))
                    }
                }
                Text(product.categoryName ?: "Uncategorized", color = CfMuted, fontSize = 12.sp)
                Text(if (product.isAvailable) "Available" else "Sold out", color = if (product.isAvailable) CfGreen else CfRed, fontSize = 12.sp)
            }
            Text("Rs. ${product.price}", color = CfBlue, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

@Composable
private fun SummaryPill(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    background: Color,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(
            modifier = Modifier.size(38.dp).clip(RoundedCornerShape(9.dp)).background(background),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(20.dp))
        }
        Column {
            Text(value, color = CfNavy, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(label, color = CfMuted, fontSize = 12.sp)
        }
    }
}

@Composable
private fun EmptyAdminState(message: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(Icons.Default.Inventory, null, tint = CfMuted, modifier = Modifier.size(48.dp))
        Text(message, fontWeight = FontWeight.SemiBold, color = CfNavy)
    }
}
