package com.neverno.neverq.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.*
import androidx.navigation.NavType
import androidx.navigation.compose.*
import com.neverno.neverq.customer.cart.CartScreen
import com.neverno.neverq.customer.menu.CustomerHomeScreen
import com.neverno.neverq.customer.menu.ProductDetailScreen
import com.neverno.neverq.customer.orders.OrderDetailScreen
import com.neverno.neverq.customer.orders.OrderHistoryScreen
import com.neverno.neverq.ui.theme.*

@Composable
fun CustomerShell(onLogout: () -> Unit) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Only show bottom nav on main tabs
    val showBottomBar = currentRoute in listOf("menu", "orders", "profile")

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                CustomerBottomBar(currentRoute = currentRoute, navController = navController)
            }
        },
        containerColor = CfSurface,
    ) { padding ->
        Box(Modifier.padding(padding)) {
            NavHost(navController = navController, startDestination = "menu") {

                composable("menu") {
                    CustomerHomeScreen(
                        onCartClick = { navController.navigate("cart") },
                        onOrdersClick = { navController.navigate("orders") },
                        onProductClick = { id -> navController.navigate("product_detail/$id") },
                        onLogout = onLogout,
                    )
                }

                composable(
                    "product_detail/{productId}",
                    arguments = listOf(navArgument("productId") { type = NavType.IntType }),
                ) {
                    ProductDetailScreen(
                        onBack = { navController.popBackStack() },
                        onProductClick = { id -> navController.navigate("product_detail/$id") },
                    )
                }

                composable("cart") {
                    CartScreen(
                        onBack = { navController.popBackStack() },
                        onOrderPlaced = { orderId ->
                            navController.navigate("order_detail/$orderId") {
                                popUpTo("cart") { inclusive = true }
                            }
                        }
                    )
                }

                composable("orders") {
                    OrderHistoryScreen(
                        onBack = { navController.popBackStack() },
                        onOrderClick = { id -> navController.navigate("order_detail/$id") },
                    )
                }

                composable(
                    "order_detail/{orderId}",
                    arguments = listOf(navArgument("orderId") { type = NavType.IntType }),
                ) { backStack ->
                    OrderDetailScreen(
                        orderId = backStack.arguments!!.getInt("orderId"),
                        onBack = { navController.popBackStack() },
                    )
                }

                composable("profile") {
                    CustomerProfileScreen(
                        onOrdersClick = { navController.navigate("orders") },
                        onWalletClick = { navController.navigate("wallet") },
                        onNotificationsClick = { navController.navigate("notifications") },
                        onLogout = onLogout,
                    )
                }

                composable("wallet") {
                    WalletScreen(onBack = { navController.popBackStack() })
                }

                composable("notifications") {
                    NotificationsScreen(onBack = { navController.popBackStack() })
                }
            }
        }
    }
}

@Composable
private fun CustomerBottomBar(currentRoute: String?, navController: NavController) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 0.dp,
        modifier = Modifier,
    ) {
        CustomerNavItem(
            icon = Icons.Default.RestaurantMenu,
            label = "Menu",
            selected = currentRoute == "menu",
            onClick = {
                navController.navigate("menu") {
                    popUpTo("menu") { inclusive = true }
                    launchSingleTop = true
                }
            }
        )
        CustomerNavItem(
            icon = Icons.Default.ReceiptLong,
            label = "Orders",
            selected = currentRoute == "orders",
            onClick = {
                navController.navigate("orders") {
                    popUpTo("menu")
                    launchSingleTop = true
                }
            }
        )
        CustomerNavItem(
            icon = Icons.Default.Person,
            label = "Profile",
            selected = currentRoute == "profile",
            onClick = {
                navController.navigate("profile") {
                    popUpTo("menu")
                    launchSingleTop = true
                }
            }
        )
    }
}

@Composable
private fun RowScope.CustomerNavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    NavigationBarItem(
        icon = { Icon(icon, null, modifier = Modifier.size(22.dp)) },
        label = { Text(label, fontSize = 11.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal) },
        selected = selected,
        onClick = onClick,
        colors = NavigationBarItemDefaults.colors(
            indicatorColor = CfBlueLight,
            selectedIconColor = CfBlue,
            selectedTextColor = CfBlue,
            unselectedIconColor = CfMuted,
            unselectedTextColor = CfMuted,
        ),
    )
}

// ── Customer Profile Screen ────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerProfileScreen(
    onOrdersClick: () -> Unit,
    onWalletClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
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
                title = { Text("My Profile", fontWeight = FontWeight.SemiBold, color = Color.White, fontSize = 18.sp) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CfNavy),
            )
        },
        containerColor = CfSurface,
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Account info card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(CfBlueLight),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Default.Person, null, tint = CfBlue, modifier = Modifier.size(28.dp))
                        }
                        Column {
                            Text("Customer Account", fontSize = 13.sp, color = CfMuted)
                            Text(
                                uiState.name.ifBlank { "Customer" },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = CfNavy,
                            )
                            if (uiState.email.isNotBlank()) {
                                Text(uiState.email, fontSize = 13.sp, color = CfMuted)
                            }
                        }
                    }
                    HorizontalDivider(color = CfBorder)
                    ProfileInfoRow(
                        icon = Icons.Default.Business,
                        label = "Company",
                        value = uiState.companyName ?: uiState.companyId?.let { "Company #$it" } ?: "Not available",
                    )
                    if (uiState.phone.isNotBlank()) {
                        ProfileInfoRow(
                            icon = Icons.Default.Phone,
                            label = "Phone",
                            value = uiState.phone,
                        )
                    }
                }
            }

            // Actions
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Column(Modifier.padding(8.dp)) {
                    ProfileAction(icon = Icons.Default.ReceiptLong, label = "My Orders", color = CfBlue, onClick = onOrdersClick)
                    HorizontalDivider(color = CfBorder, modifier = Modifier.padding(horizontal = 12.dp))
                    ProfileAction(icon = Icons.Default.AccountBalanceWallet, label = "My Wallet", color = CfGreen, onClick = onWalletClick)
                    HorizontalDivider(color = CfBorder, modifier = Modifier.padding(horizontal = 12.dp))
                    ProfileAction(icon = Icons.Default.Notifications, label = "Notifications", color = CfOrange, onClick = onNotificationsClick)
                    HorizontalDivider(color = CfBorder, modifier = Modifier.padding(horizontal = 12.dp))
                    ProfileAction(icon = Icons.Default.Logout, label = "Sign Out", color = CfRed) {
                        showLogoutDialog = true
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(CfBlueLight),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, null, tint = CfBlue, modifier = Modifier.size(18.dp))
        }
        Column {
            Text(label, fontSize = 12.sp, color = CfMuted)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = CfText)
        }
    }
}

@Composable
private fun ProfileAction(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
        }
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = CfNavy, modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, null, tint = CfMuted, modifier = Modifier.size(18.dp))
    }
}
