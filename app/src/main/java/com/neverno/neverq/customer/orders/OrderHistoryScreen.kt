package com.neverno.neverq.customer.orders

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.neverno.neverq.core.models.OrderListItem
import com.neverno.neverq.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderHistoryScreen(
    onBack: () -> Unit,
    onOrderClick: (Int) -> Unit,
    viewModel: OrderViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Orders", color = Color.White, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CfNavy),
            )
        },
        containerColor = CfSurface,
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when {
                uiState.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center), color = CfBlue)
                uiState.orders.isEmpty() -> EmptyOrdersState(Modifier.align(Alignment.Center))
                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(uiState.orders, key = { it.id }) { order ->
                        OrderListCard(order = order, onClick = { onOrderClick(order.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyOrdersState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(Icons.Default.ReceiptLong, null, modifier = Modifier.size(64.dp), tint = CfMuted)
        Spacer(Modifier.height(8.dp))
        Text("No orders yet", style = MaterialTheme.typography.bodyLarge, color = CfText)
    }
}

@Composable
fun OrderListCard(order: OrderListItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("#${order.orderNumber}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = CfNavy)
                    StatusBadge(status = order.statusLabel)
                }
                order.createdAt?.let {
                    Text(it.take(10), style = MaterialTheme.typography.bodySmall, color = CfMuted)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${order.itemCount} items", style = MaterialTheme.typography.bodySmall, color = CfMuted)
                    Text("Rs. ${order.totalAmount}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = CfText)
                }
            }
            Icon(Icons.Default.ChevronRight, null, tint = CfMuted, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun StatusBadge(status: String) {
    val colors = orderStatusColors(status)
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = colors.background,
    ) {
        Text(
            status,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = colors.foreground,
        )
    }
}

private data class BadgeColors(val foreground: Color, val background: Color)

private fun orderStatusColors(status: String): BadgeColors {
    return when (status.lowercase()) {
        "pending" -> BadgeColors(CfOrange, CfOrangeLight)
        "confirmed", "preparing" -> BadgeColors(CfBlue, CfBlueLight)
        "ready", "delivered" -> BadgeColors(CfGreen, CfGreenLight)
        "cancelled", "canceled" -> BadgeColors(CfRed, CfRedLight)
        else -> BadgeColors(CfMuted, CfBorder)
    }
}
