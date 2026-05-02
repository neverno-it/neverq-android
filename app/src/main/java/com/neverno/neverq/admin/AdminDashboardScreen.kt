package com.neverno.neverq.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.neverno.neverq.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    onOrdersClick: () -> Unit,
    onLogout: () -> Unit,
    viewModel: AdminViewModel = hiltViewModel(),
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
                    Text(
                        "Dashboard",
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        fontSize = 18.sp,
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.loadDashboard() }) {
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
        Box(Modifier.padding(padding).fillMaxSize()) {
            if (uiState.isLoading && uiState.stats == null) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = CfBlue,
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    uiState.stats?.let { stats ->
                        item {
                            Text(
                                "Today's Overview",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = CfMuted,
                                letterSpacing = 0.5.sp,
                            )
                        }
                        item {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                StatCard(
                                    title = "Today Orders",
                                    value = stats.todayWebOrders.toString(),
                                    icon = Icons.Default.ShoppingCart,
                                    iconBg = CfBlueLight,
                                    iconTint = CfBlue,
                                    modifier = Modifier.weight(1f),
                                )
                                StatCard(
                                    title = "Today Revenue",
                                    value = "₹${stats.todayWebRevenue}",
                                    icon = Icons.Default.CurrencyRupee,
                                    iconBg = CfGreenLight,
                                    iconTint = CfGreen,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                        item {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                StatCard(
                                    title = "Pending",
                                    value = stats.pendingOrders.toString(),
                                    icon = Icons.Default.Pending,
                                    iconBg = CfOrangeLight,
                                    iconTint = CfOrange,
                                    modifier = Modifier.weight(1f),
                                )
                                StatCard(
                                    title = "Customers",
                                    value = stats.activeCustomers.toString(),
                                    icon = Icons.Default.People,
                                    iconBg = CfPurpleLight,
                                    iconTint = CfPurple,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                        item {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                StatCard(
                                    title = "POS Orders",
                                    value = stats.todayPosOrders.toString(),
                                    icon = Icons.Default.PointOfSale,
                                    iconBg = CfBlueLight,
                                    iconTint = CfBlueDark,
                                    modifier = Modifier.weight(1f),
                                )
                                StatCard(
                                    title = "POS Revenue",
                                    value = "₹${stats.todayPosRevenue}",
                                    icon = Icons.Default.AttachMoney,
                                    iconBg = CfRedLight,
                                    iconTint = CfRed,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }

                        uiState.weeklyRevenue.let { weekly ->
                            if (weekly.isNotEmpty()) {
                                item {
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        "Weekly Revenue",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = CfMuted,
                                        letterSpacing = 0.5.sp,
                                    )
                                }
                                item {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                    ) {
                                        Column(Modifier.padding(16.dp)) {
                                            weekly.forEach { rev ->
                                                Row(
                                                    Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                ) {
                                                    Text(rev.date, fontSize = 13.sp, color = CfMuted)
                                                    Text(
                                                        "₹${rev.revenue}",
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = CfNavy,
                                                    )
                                                }
                                                if (weekly.last() != rev) HorizontalDivider(color = CfBorder)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(Modifier.height(4.dp))
                            Button(
                                onClick = onOrdersClick,
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CfNavy),
                            ) {
                                Icon(Icons.Default.FormatListBulleted, null)
                                Spacer(Modifier.width(8.dp))
                                Text("View All Orders", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.shadow(
            elevation = 4.dp,
            shape = RoundedCornerShape(14.dp),
            ambientColor = CfNavyDeep.copy(alpha = 0.10f),
        ),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, null, tint = iconTint, modifier = Modifier.size(22.dp))
            }
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = CfNavy)
            Text(title, fontSize = 12.sp, color = CfMuted, fontWeight = FontWeight.Medium)
        }
    }
}
