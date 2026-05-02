package com.neverno.neverq.kitchen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.neverno.neverq.core.models.KitchenOrder
import com.neverno.neverq.ui.theme.*

private val STATUS_LABELS = mapOf(1 to "Pending", 2 to "Confirmed", 3 to "Preparing", 4 to "Ready")
private val NEXT_STATUS = mapOf(1 to 2, 2 to 3, 3 to 4)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KitchenScreen(
    onLogout: () -> Unit,
    viewModel: KitchenViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.navigateTo) {
        if (uiState.navigateTo == "login") onLogout()
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Sign Out", fontWeight = FontWeight.SemiBold) },
            text = { Text("Are you sure you want to sign out?") },
            confirmButton = {
                Button(
                    onClick = { showLogoutDialog = false; viewModel.logout() },
                    colors = ButtonDefaults.buttonColors(containerColor = CfRed),
                ) { Text("Sign Out") }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel") }
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Kitchen Display", fontWeight = FontWeight.SemiBold, color = Color.White, fontSize = 18.sp)
                        if (uiState.isOffline) {
                            Text("Offline — cached data", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                        }
                    }
                },
                actions = {
                    if (uiState.isOffline) {
                        Icon(Icons.Default.WifiOff, "Offline", tint = CfOrange, modifier = Modifier.padding(end = 4.dp))
                    }
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, "Refresh", tint = Color.White)
                    }
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(Icons.Default.Logout, "Sign Out", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CfNavy),
            )
        },
        containerColor = CfSurface,
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when {
                uiState.isLoading && uiState.orders.isEmpty() ->
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = CfBlue)

                uiState.error != null && uiState.orders.isEmpty() ->
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Icon(Icons.Default.WifiOff, null, tint = CfMuted, modifier = Modifier.size(48.dp))
                        Text(uiState.error!!, color = CfMuted, fontSize = 14.sp)
                        Button(onClick = { viewModel.refresh() }, colors = ButtonDefaults.buttonColors(containerColor = CfBlue)) {
                            Text("Retry")
                        }
                    }

                uiState.orders.isEmpty() ->
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(Icons.Default.CheckCircle, null, tint = CfGreen, modifier = Modifier.size(56.dp))
                        Text("All caught up!", fontWeight = FontWeight.SemiBold, color = CfNavy, fontSize = 16.sp)
                        Text("No active orders right now.", color = CfMuted, fontSize = 13.sp)
                    }

                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(uiState.orders, key = { it.id }) { order ->
                        KitchenOrderCard(
                            order = order,
                            onStatusUpdate = { newStatus -> viewModel.updateStatus(order.id, newStatus) },
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

    val (statusBg, statusColor, nextBtnColor) = when (order.orderStatus) {
        1 -> Triple(CfRedLight, CfRed, CfOrange)
        2 -> Triple(CfOrangeLight, CfOrange, CfBlue)
        3 -> Triple(CfBlueLight, CfBlue, CfGreen)
        else -> Triple(CfGreenLight, CfGreen, CfGreen)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        "#${order.orderNumber}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = CfNavy,
                    )
                    Text(
                        order.displayCustomerName,
                        fontSize = 13.sp,
                        color = CfMuted,
                    )
                    if (order.displayCustomerPhone.isNotEmpty()) {
                        Text(order.displayCustomerPhone, fontSize = 12.sp, color = CfMuted)
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

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = CfBorder)

            // Items
            order.items.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(item.productName, fontSize = 14.sp, color = CfText, modifier = Modifier.weight(1f))
                    Text(
                        "× ${item.qty}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = CfBlue,
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "₹${order.totalAmount}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = CfNavy,
                )
                if (nextStatus != null) {
                    Button(
                        onClick = { onStatusUpdate(nextStatus) },
                        colors = ButtonDefaults.buttonColors(containerColor = nextBtnColor),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    ) {
                        Text("Mark ${STATUS_LABELS[nextStatus]}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(CfGreenLight)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                    ) {
                        Text("Completed", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = CfGreen)
                    }
                }
            }
        }
    }
}
