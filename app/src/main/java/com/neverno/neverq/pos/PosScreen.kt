package com.neverno.neverq.pos

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.neverno.neverq.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosScreen(
    onLogout: () -> Unit,
    viewModel: PosViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var showPaymentDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.navigateTo) {
        if (uiState.navigateTo == "login") onLogout()
    }

    val cartTotal = uiState.cart.sumOf { it.total }
    val cartItemCount = uiState.cart.sumOf { it.qty }

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
                        Text("POS Terminal", fontWeight = FontWeight.SemiBold, color = Color.White, fontSize = 18.sp)
                        if (uiState.isOffline) {
                            Text("Offline — cached products", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                        }
                    }
                },
                actions = {
                    if (uiState.isOffline) {
                        Icon(Icons.Default.WifiOff, "Offline", tint = CfOrange, modifier = Modifier.padding(end = 4.dp))
                    }
                    IconButton(onClick = { viewModel.loadProducts() }) {
                        Icon(Icons.Default.Refresh, "Refresh", tint = Color.White)
                    }
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(Icons.Default.Logout, "Sign Out", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CfNavy),
            )
        },
        bottomBar = {
            if (uiState.cart.isNotEmpty()) {
                Surface(
                    shadowElevation = 12.dp,
                    color = Color.White,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text("$cartItemCount item${if (cartItemCount != 1) "s" else ""}", fontSize = 12.sp, color = CfMuted)
                            Text(
                                "₹${"%.2f".format(cartTotal)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = CfNavy,
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { viewModel.clearCart() },
                                shape = RoundedCornerShape(8.dp),
                            ) { Text("Clear", color = CfMuted) }
                            Button(
                                onClick = { showPaymentDialog = true },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CfGreen),
                            ) {
                                Icon(Icons.Default.CurrencyRupee, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Charge", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        },
        containerColor = CfSurface,
    ) { padding ->
        if (uiState.isLoading && uiState.products.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = CfBlue)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 140.dp),
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(uiState.products) { product ->
                    ProductTile(
                        name = product.name,
                        price = product.price,
                        qty = uiState.cart.find { it.name == product.name }?.qty ?: 0,
                        onClick = { viewModel.addToCart(product) },
                    )
                }
            }
        }
    }
}

@Composable
fun ProductTile(name: String, price: String, qty: Int, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Box(Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(12.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = CfNavy, maxLines = 2)
                Spacer(Modifier.height(4.dp))
                Text("₹$price", fontSize = 13.sp, color = CfBlue, fontWeight = FontWeight.Bold)
            }
            if (qty > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(22.dp)
                        .clip(RoundedCornerShape(50))
                        .background(CfBlue),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("$qty", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun PaymentTypeDialog(onConfirm: (Int) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Payment Method", fontWeight = FontWeight.SemiBold, color = CfNavy) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    Triple(1, "Cash", Icons.Default.Money),
                    Triple(2, "Card", Icons.Default.CreditCard),
                    Triple(3, "UPI", Icons.Default.QrCode),
                ).forEach { (type, label, icon) ->
                    Button(
                        onClick = { onConfirm(type) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CfNavy),
                    ) {
                        Icon(icon, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(label, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = CfMuted) } },
        shape = RoundedCornerShape(16.dp),
    )
}

@Composable
fun PosReceiptDialog(order: com.neverno.neverq.core.models.PosOrder, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.CheckCircle, null, tint = CfGreen, modifier = Modifier.size(22.dp))
                Text("Receipt — #${order.orderNumber}", fontWeight = FontWeight.SemiBold, color = CfNavy)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                order.items.forEach { item ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${item.productName} ×${item.qty}", fontSize = 13.sp, color = CfText, modifier = Modifier.weight(1f))
                        Text("₹${item.amount}", fontSize = 13.sp, color = CfNavy, fontWeight = FontWeight.Medium)
                    }
                }
                HorizontalDivider(color = CfBorder)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total", fontWeight = FontWeight.Bold, color = CfNavy)
                    Text("₹${order.totalAmount}", fontWeight = FontWeight.Bold, color = CfNavy)
                }
                Text(order.paymentLabel, fontSize = 12.sp, color = CfMuted)
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = CfBlue),
                shape = RoundedCornerShape(8.dp),
            ) { Text("Done", fontWeight = FontWeight.SemiBold) }
        },
        shape = RoundedCornerShape(16.dp),
    )
}
