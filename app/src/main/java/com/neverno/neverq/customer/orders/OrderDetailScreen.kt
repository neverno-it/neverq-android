package com.neverno.neverq.customer.orders

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    orderId: Int,
    onBack: () -> Unit,
    viewModel: OrderViewModel = hiltViewModel(),
) {
    val uiState by viewModel.detailState.collectAsState()

    LaunchedEffect(orderId) { viewModel.loadOrderDetail(orderId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.order?.let { "#${it.orderNumber}" } ?: "Order Detail") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
            )
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when {
                uiState.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                uiState.error != null -> Text(uiState.error!!, Modifier.align(Alignment.Center))
                uiState.order != null -> {
                    val order = uiState.order!!
                    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        item {
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Status", style = MaterialTheme.typography.bodySmall)
                                        Text(order.statusLabel, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    }
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Payment", style = MaterialTheme.typography.bodySmall)
                                        Text(order.paymentMode.uppercase())
                                    }
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Subtotal", style = MaterialTheme.typography.bodySmall)
                                        Text("₹${order.subtotal}")
                                    }
                                    if (order.couponDiscount != "0.00") {
                                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Coupon Discount", style = MaterialTheme.typography.bodySmall)
                                            Text("- ₹${order.couponDiscount}", color = MaterialTheme.colorScheme.tertiary)
                                        }
                                    }
                                    HorizontalDivider()
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Total", fontWeight = FontWeight.Bold)
                                        Text("₹${order.totalAmount}", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                        items(order.items) { item ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("${item.productName} × ${item.qty}", style = MaterialTheme.typography.bodyMedium)
                                Text("₹${item.lineTotal}", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}
