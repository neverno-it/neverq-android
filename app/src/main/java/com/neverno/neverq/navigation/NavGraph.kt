package com.neverno.neverq.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.neverno.neverq.admin.AdminDashboardScreen
import com.neverno.neverq.admin.AdminOrdersScreen
import com.neverno.neverq.auth.LoginScreen
import com.neverno.neverq.customer.menu.CustomerHomeScreen
import com.neverno.neverq.customer.cart.CartScreen
import com.neverno.neverq.customer.orders.OrderHistoryScreen
import com.neverno.neverq.customer.orders.OrderDetailScreen
import com.neverno.neverq.kitchen.KitchenScreen
import com.neverno.neverq.pos.PosScreen

@Composable
fun NeverQNavGraph(navController: NavHostController, startDestination: String) {
    NavHost(navController = navController, startDestination = startDestination) {

        composable("login") {
            LoginScreen(
                onNavigateTo = { route ->
                    navController.navigate(route) {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        // ── Customer ──────────────────────────────────────────────────────────
        composable("customer_home") {
            CustomerHomeScreen(
                onCartClick = { navController.navigate("cart") },
                onOrdersClick = { navController.navigate("order_history") },
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

        composable("order_history") {
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

        // ── Kitchen ───────────────────────────────────────────────────────────
        composable("kitchen") {
            KitchenScreen()
        }

        // ── POS ───────────────────────────────────────────────────────────────
        composable("pos") {
            PosScreen()
        }

        // ── Admin ─────────────────────────────────────────────────────────────
        composable("admin_dashboard") {
            AdminDashboardScreen(
                onOrdersClick = { navController.navigate("admin_orders") },
            )
        }

        composable("admin_orders") {
            AdminOrdersScreen(onBack = { navController.popBackStack() })
        }
    }
}
