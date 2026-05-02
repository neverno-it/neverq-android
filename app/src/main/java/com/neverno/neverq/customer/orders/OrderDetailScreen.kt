package com.neverno.neverq.customer.orders

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.neverno.neverq.core.models.OrderDetail
import com.neverno.neverq.core.models.OrderItemDetail
import com.neverno.neverq.ui.theme.*

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
                title = {
                    Text(uiState.order?.let { "#${it.orderNumber}" } ?: "Order Detail", color = Color.White, fontWeight = FontWeight.SemiBold)
                },
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
                uiState.error != null -> Text(uiState.error!!, Modifier.align(Alignment.Center), color = CfRed)
                uiState.order != null -> OrderDetailContent(order = uiState.order!!)
            }
        }
    }
}

@Composable
private fun OrderDetailContent(order: OrderDetail) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { OrderStatusCard(order) }
        item { ItemsCard(order.items) }
        item { PaymentSummaryCard(order) }
    }
}

@Composable
private fun OrderStatusCard(order: OrderDetail) {
    val colors = detailStatusColors(order.statusLabel)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("Order status", style = MaterialTheme.typography.bodySmall, color = CfMuted)
                    Text(order.statusLabel, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = colors.foreground)
                }
                Surface(shape = RoundedCornerShape(999.dp), color = colors.background) {
                    Text(
                        order.paymentStatus,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.foreground,
                    )
                }
            }
            HorizontalDivider(color = CfBorder)
            DetailLine("Order number", "#${order.orderNumber}")
            order.createdAt?.let { DetailLine("Placed on", it.take(10)) }
            DetailLine("Payment mode", order.paymentMode.uppercase())
        }
    }
}

@Composable
private fun ItemsCard(items: List<OrderItemDetail>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.ReceiptLong, null, tint = CfBlue, modifier = Modifier.size(20.dp))
                Text("Items", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = CfNavy)
            }
            HorizontalDivider(color = CfBorder)
            items.forEachIndexed { index, item ->
                OrderItemRow(item)
                if (index != items.lastIndex) HorizontalDivider(color = CfBorder)
            }
        }
    }
}

@Composable
private fun OrderItemRow(item: OrderItemDetail) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(item.productName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = CfText)
            Text("Qty ${item.qty} x Rs. ${item.price}", style = MaterialTheme.typography.bodySmall, color = CfMuted)
        }
        Text("Rs. ${item.lineTotal}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = CfNavy)
    }
}

@Composable
private fun PaymentSummaryCard(order: OrderDetail) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Payment summary", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = CfNavy)
            HorizontalDivider(color = CfBorder)
            DetailLine("Subtotal", "Rs. ${order.subtotal}")
            DetailLine("Wallet used", "- Rs. ${order.walletUsed}", valueColor = CfGreen)
            DetailLine("Coupon discount", "- Rs. ${order.couponDiscount}", valueColor = CfOrange)
            HorizontalDivider(color = CfBorder)
            DetailLine("Total", "Rs. ${order.totalAmount}", bold = true)
        }
    }
}

@Composable
private fun DetailLine(
    label: String,
    value: String,
    valueColor: Color = CfText,
    bold: Boolean = false,
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = CfMuted)
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            color = if (bold) CfNavy else valueColor,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.SemiBold,
        )
    }
}

private data class DetailBadgeColors(val foreground: Color, val background: Color)

private fun detailStatusColors(status: String): DetailBadgeColors {
    return when (status.lowercase()) {
        "pending" -> DetailBadgeColors(CfOrange, CfOrangeLight)
        "confirmed", "preparing" -> DetailBadgeColors(CfBlue, CfBlueLight)
        "ready", "delivered" -> DetailBadgeColors(CfGreen, CfGreenLight)
        "cancelled", "canceled" -> DetailBadgeColors(CfRed, CfRedLight)
        else -> DetailBadgeColors(CfMuted, CfBorder)
    }
}
