package com.neverno.neverq.pos

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosScreen(viewModel: PosViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    var showPaymentDialog by remember { mutableStateOf(false) }

    val cartTotal = uiState.cart.sumOf { it.total }

    if (uiState.lastOrder != null) {
        PosReceiptDialog(order = uiState.lastOrder!!, onDismiss = { viewModel.dismissReceipt() })
    }

    if (showPaymentDialog) {
        PaymentTypeDialog(
            onConfirm = { paymentType ->
                showPaymentDialog = false
                viewModel.placeOrder(paymentType)
            },
            onDismiss = { showPaymentDialog = false },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("POS Terminal") },
                actions = {
                    if (uiState.isOffline) Icon(Icons.Default.WifiOff, contentDescription = "Offline")
                    IconButton(onClick = { viewModel.loadProducts() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        },
        bottomBar = {
            if (uiState.cart.isNotEmpty()) {
                Surface(shadowElevation = 8.dp) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text("${uiState.cart.size} items", style = MaterialTheme.typography.bodySmall)
                            Text(
                                "₹${"%.2f".format(cartTotal)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { viewModel.clearCart() }) { Text("Clear") }
                            Button(onClick = { showPaymentDialog = true }) { Text("Charge") }
                        }
                    }
                }
            }
        }
    ) { padding ->
        Row(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Product grid (left / full on phone)
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 150.dp),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(uiState.products) { product ->
                    ProductTile(
                        name = product.name,
                        price = product.price,
                        onClick = { viewModel.addToCart(product) },
                    )
                }
            }

            // Cart sidebar (visible on wide screens / tablets)
            if (uiState.cart.isNotEmpty()) {
                Divider(modifier = Modifier.fillMaxHeight().width(1.dp))
                LazyColumn(
                    modifier = Modifier.width(240.dp),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(uiState.cart) { item ->
                        CartLineItem(
                            item = item,
                            onAdd = { viewModel.addToCart(com.neverno.neverq.core.models.PosProduct("", item.name, item.price.toString())) },
                            onRemove = { viewModel.removeFromCart(item.name) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProductTile(name: String, price: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(2.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text("₹$price", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun CartLineItem(item: PosCartItem, onAdd: () -> Unit, onRemove: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(item.name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onRemove, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Remove, contentDescription = "Remove", modifier = Modifier.size(14.dp))
            }
            Text("${item.qty}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            IconButton(onClick = onAdd, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
fun PaymentTypeDialog(onConfirm: (Int) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Payment Method") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(1 to "Cash", 2 to "Card", 3 to "UPI").forEach { (type, label) ->
                    OutlinedButton(
                        onClick = { onConfirm(type) },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text(label) }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

@Composable
fun PosReceiptDialog(order: com.neverno.neverq.core.models.PosOrder, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Receipt — #${order.orderNumber}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                order.items.forEach { item ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${item.productName} ×${item.qty}", style = MaterialTheme.typography.bodySmall)
                        Text("₹${item.amount}", style = MaterialTheme.typography.bodySmall)
                    }
                }
                HorizontalDivider()
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total", fontWeight = FontWeight.Bold)
                    Text("₹${order.totalAmount}", fontWeight = FontWeight.Bold)
                }
                Text("Payment: ${order.paymentLabel}", style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Done") }
        }
    )
}
