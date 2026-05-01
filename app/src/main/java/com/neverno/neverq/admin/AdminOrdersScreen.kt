package com.neverno.neverq.admin

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
import com.neverno.neverq.kitchen.StatusChip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminOrdersScreen(
    onBack: () -> Unit,
    viewModel: AdminViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var filterStatus by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(filterStatus) { viewModel.loadOrders(filterStatus) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("All Orders") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            // Status filter chips
            val statuses = listOf(null to "All", 1 to "Pending", 2 to "Confirmed", 3 to "Preparing", 4 to "Ready", 5 to "Delivered")
            ScrollableTabRow(selectedTabIndex = statuses.indexOfFirst { it.first == filterStatus }.coerceAtLeast(0)) {
                statuses.forEach { (status, label) ->
                    Tab(
                        selected = filterStatus == status,
                        onClick = { filterStatus = status },
                        text = { Text(label) },
                    )
                }
            }

            Box(Modifier.fillMaxSize()) {
                when {
                    uiState.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                    uiState.orders.isEmpty() -> Text("No orders found.", Modifier.align(Alignment.Center))
                    else -> LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(uiState.orders, key = { it.id }) { order ->
                            Card(Modifier.fillMaxWidth()) {
                                Row(
                                    Modifier.padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        Text("#${order.orderNumber}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        Text("${order.itemCount} items • ₹${order.totalAmount}", style = MaterialTheme.typography.bodySmall)
                                        order.createdAt?.let { Text(it.take(16), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                                    }
                                    StatusChip(order.statusLabel, order.orderStatus)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
