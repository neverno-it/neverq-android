package com.neverno.neverq.kitchen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.neverno.neverq.core.models.KitchenOrder

private val STATUS_LABELS = mapOf(
    1 to "Pending",
    2 to "Confirmed",
    3 to "Preparing",
    4 to "Ready",
)
private val NEXT_STATUS = mapOf(1 to 2, 2 to 3, 3 to 4)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KitchenScreen(viewModel: KitchenViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kitchen Display") },
                actions = {
                    if (uiState.isOffline) Icon(Icons.Default.WifiOff, contentDescription = "Offline")
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when {
                uiState.isLoading && uiState.orders.isEmpty() ->
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

                uiState.error != null && uiState.orders.isEmpty() ->
                    Text(
                        uiState.error!!,
                        modifier = Modifier.align(Alignment.Center).padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge,
                    )

                uiState.orders.isEmpty() ->
                    Text(
                        "No active orders",
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodyLarge,
                    )

                else -> LazyColumn(
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(uiState.orders, key = { it.id }) { order ->
                        KitchenOrderCard(
                            order = order,
                            onStatusUpdate = { newStatus ->
                                viewModel.updateStatus(order.id, newStatus)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun KitchenOrderCard(order: KitchenOrder, onStatusUpdate: (Int) -> Unit) {
    val nextStatus = NEXT_STATUS[order.orderStatus]

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "#${order.orderNumber}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                StatusChip(order.statusLabel, order.orderStatus)
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = order.displayCustomerName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (order.displayCustomerPhone.isNotEmpty()) {
                Text(
                    text = order.displayCustomerPhone,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            order.items.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(item.productName, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "× ${item.qty}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "₹${order.totalAmount}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                if (nextStatus != null) {
                    Button(
                        onClick = { onStatusUpdate(nextStatus) },
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    ) {
                        Text("Mark ${STATUS_LABELS[nextStatus]}")
                    }
                }
            }
        }
    }
}

@Composable
fun StatusChip(label: String, status: Int) {
    val color = when (status) {
        1 -> MaterialTheme.colorScheme.error
        2 -> MaterialTheme.colorScheme.tertiary
        3 -> MaterialTheme.colorScheme.primary
        4 -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.outline
    }
    Surface(
        shape = MaterialTheme.shapes.small,
        color = color.copy(alpha = 0.15f),
        contentColor = color,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
