package com.neverno.neverq.customer.cart

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
fun CartScreen(
    onBack: () -> Unit,
    onOrderPlaced: (Int) -> Unit,
    viewModel: CartViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCheckoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.placedOrderId) {
        uiState.placedOrderId?.let { onOrderPlaced(it) }
    }

    if (showCheckoutDialog) {
        CheckoutDialog(
            subtotal = uiState.subtotal,
            onConfirm = { paymentMode ->
                showCheckoutDialog = false
                viewModel.checkout(paymentMode)
            },
            onDismiss = { showCheckoutDialog = false },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cart") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }
                },
                actions = {
                    if (uiState.items.isNotEmpty()) {
                        TextButton(onClick = { viewModel.clearCart() }) { Text("Clear") }
                    }
                }
            )
        },
        bottomBar = {
            if (uiState.items.isNotEmpty()) {
                Surface(shadowElevation = 8.dp) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text("Subtotal", style = MaterialTheme.typography.bodySmall)
                            Text(
                                "₹${uiState.subtotal}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Button(
                            onClick = { showCheckoutDialog = true },
                            enabled = !uiState.isLoading,
                        ) {
                            Text("Place Order")
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when {
                uiState.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                uiState.items.isEmpty() -> Column(
                    Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(Icons.Default.ShoppingCart, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
                    Spacer(Modifier.height(8.dp))
                    Text("Your cart is empty", style = MaterialTheme.typography.bodyLarge)
                }
                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
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
                            }
                        )
                    }
                }
            }
        }
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
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text("₹$price each", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Remove, null, modifier = Modifier.size(16.dp))
                }
                Text("$qty", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                IconButton(onClick = onAdd, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                }
            }
            Text(
                "₹$lineTotal",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

@Composable
fun CheckoutDialog(subtotal: String, onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirm Order — ₹$subtotal") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Select payment method:")
                listOf("online" to "Online Payment", "cash" to "Cash on Delivery", "monthly" to "Monthly Billing").forEach { (mode, label) ->
                    OutlinedButton(
                        onClick = { onConfirm(mode) },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text(label) }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
