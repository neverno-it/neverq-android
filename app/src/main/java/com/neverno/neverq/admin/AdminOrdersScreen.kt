package com.neverno.neverq.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.neverno.neverq.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminOrdersScreen(
    onBack: () -> Unit,
    viewModel: AdminViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var filterStatus by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(filterStatus) { viewModel.loadOrders(filterStatus) }

    val statuses = listOf(
        null to "All",
        1 to "Pending",
        2 to "Confirmed",
        3 to "Preparing",
        4 to "Ready",
        5 to "Delivered",
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("All Orders", fontWeight = FontWeight.SemiBold, color = Color.White, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CfNavy),
            )
        },
        containerColor = CfSurface,
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            // Status tabs
            ScrollableTabRow(
                selectedTabIndex = statuses.indexOfFirst { it.first == filterStatus }.coerceAtLeast(0),
                containerColor = Color.White,
                contentColor = CfBlue,
                edgePadding = 8.dp,
            ) {
                statuses.forEach { (status, label) ->
                    Tab(
                        selected = filterStatus == status,
                        onClick = { filterStatus = status },
                        text = {
                            Text(
                                label,
                                fontSize = 13.sp,
                                fontWeight = if (filterStatus == status) FontWeight.SemiBold else FontWeight.Normal,
                            )
                        },
                        selectedContentColor = CfBlue,
                        unselectedContentColor = CfMuted,
                    )
                }
            }

            Box(Modifier.fillMaxSize()) {
                when {
                    uiState.isLoading -> CircularProgressIndicator(
                        Modifier.align(Alignment.Center),
                        color = CfBlue,
                    )
                    uiState.orders.isEmpty() -> Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text("No orders found", fontWeight = FontWeight.SemiBold, color = CfNavy, fontSize = 16.sp)
                        Text("Try a different filter", fontSize = 13.sp, color = CfMuted)
                    }
                    else -> LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(uiState.orders, key = { it.id }) { order ->
                            OrderListCard(order = order)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderListCard(order: com.neverno.neverq.core.models.OrderListItem) {
    val (statusBg, statusColor) = when (order.orderStatus) {
        1 -> CfRedLight to CfRed
        2 -> CfOrangeLight to CfOrange
        3 -> CfBlueLight to CfBlue
        4 -> CfGreenLight to CfGreen
        5 -> CfGreenLight to CfGreen
        else -> CfBorder to CfMuted
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    "#${order.orderNumber}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = CfNavy,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "${order.itemCount} items  •  ₹${order.totalAmount}",
                    fontSize = 13.sp,
                    color = CfText,
                )
                order.createdAt?.let {
                    Text(it.take(16).replace("T", "  "), fontSize = 12.sp, color = CfMuted)
                }
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(statusBg)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Text(order.statusLabel, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = statusColor)
            }
        }
    }
}
