package com.neverno.neverq.admin

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.*
import com.neverno.neverq.ui.theme.*

@Composable
fun AdminShell(onLogout: () -> Unit) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White, tonalElevation = 0.dp) {
                AdminNavItem(
                    icon = Icons.Default.Dashboard,
                    label = "Dashboard",
                    selected = currentRoute == "dashboard",
                    onClick = {
                        navController.navigate("dashboard") {
                            popUpTo("dashboard") { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
                AdminNavItem(
                    icon = Icons.Default.ShoppingBag,
                    label = "Orders",
                    selected = currentRoute == "orders",
                    onClick = {
                        navController.navigate("orders") {
                            popUpTo("dashboard")
                            launchSingleTop = true
                        }
                    }
                )
            }
        },
        containerColor = CfSurface,
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(navController = navController, startDestination = "dashboard") {
                composable("dashboard") {
                    AdminDashboardScreen(
                        onOrdersClick = { navController.navigate("orders") },
                        onLogout = onLogout,
                    )
                }
                composable("orders") {
                    AdminOrdersScreen(onBack = { navController.popBackStack() })
                }
            }
        }
    }
}

@Composable
private fun RowScope.AdminNavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    NavigationBarItem(
        icon = { Icon(icon, null) },
        label = {
            Text(
                label,
                fontSize = 11.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            )
        },
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
