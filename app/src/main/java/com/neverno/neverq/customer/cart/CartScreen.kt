package com.neverno.neverq.customer.cart

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.neverno.neverq.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    onBack: () -> Unit,
    onOrderPlaced: (Int) -> Unit,
    viewModel: CartViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.placedOrderId) {
        uiState.placedOrderId?.let { onOrderPlaced(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Cart", color = Color.White, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    }
                },
                actions = {
                    if (uiState.items.isNotEmpty()) {
                        TextButton(onClick = { viewModel.clearCart() }) {
                            Text("Clear", color = Color.White)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CfNavy),
            )
        },
        bottomBar = {
            if (uiState.items.isNotEmpty()) {
                Surface(color = CfSurface, shadowElevation = 8.dp) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            SummaryRow(label = "Subtotal", value = "Rs. ${uiState.subtotal}", muted = true)
                            HorizontalDivider(color = CfBorder)
                            SummaryRow(label = "Total", value = "Rs. ${uiState.subtotal}", bold = true)
                            Button(
                                onClick = { viewModel.checkout() },
                                enabled = !uiState.isLoading,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CfBlue,
                                    contentColor = Color.White,
                                    disabledContainerColor = CfBlueLight,
                                    disabledContentColor = CfMuted,
                                ),
                            ) {
                                Text("Place Order", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        },
        containerColor = CfSurface,
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when {
                uiState.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center), color = CfBlue)
                uiState.items.isEmpty() -> EmptyCartState(Modifier.align(Alignment.Center))
                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(uiState.items, key = { it.id }) { item ->
                        CartItemRow(
                            name = item.product.name,
                            price = item.product.price,
                            qty = item.qty,
                            lineTotal = item.lineTotal,
                            onAdd = { viewModel.updateQty(item.id, item.qty + 1) },
                            onRemove = {
                                if (item.qty > 1) viewModel.updateQty(item.id, item.qty - 1)
                                else viewModel.removeItem(item.id)
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyCartState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(Icons.Default.ShoppingCart, null, modifier = Modifier.size(64.dp), tint = CfMuted)
        Spacer(Modifier.height(8.dp))
        Text("Your cart is empty", style = MaterialTheme.typography.bodyLarge, color = CfText)
    }
}

@Composable
fun CartItemRow(
    name: String,
    price: String,
    qty: Int,
    lineTotal: String,
    onAdd: () -> Unit,
    onRemove: () -> Unit,
) {
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
            Column(modifier = Modifier.weight(1f)) {
                Text(name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = CfText)
                Spacer(Modifier.height(3.dp))
                Text("Rs. $price each", style = MaterialTheme.typography.bodySmall, color = CfMuted)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.height(36.dp),
            ) {
                IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Remove, null, modifier = Modifier.size(16.dp), tint = CfBlue)
                }
                Text("$qty", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = CfNavy)
                IconButton(onClick = onAdd, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp), tint = CfBlue)
                }
            }
            Text(
                "Rs. $lineTotal",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = CfNavy,
            )
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    muted: Boolean = false,
    bold: Boolean = false,
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (muted) CfMuted else CfText,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            color = CfNavy,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.SemiBold,
        )
    }
}
