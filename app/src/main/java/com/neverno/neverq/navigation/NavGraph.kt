package com.neverno.neverq.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.neverno.neverq.admin.AdminShell
import com.neverno.neverq.auth.LoginScreen
import com.neverno.neverq.customer.CustomerShell
import com.neverno.neverq.kitchen.KitchenScreen
import com.neverno.neverq.pos.PosScreen

@Composable
fun NeverQNavGraph(navController: NavHostController, startDestination: String) {

    // Shared logout: clears back stack and returns to login
    val onLogout: () -> Unit = {
        navController.navigate("login") {
            popUpTo(0) { inclusive = true }
        }
    }

    NavHost(navController = navController, startDestination = startDestination) {

        // ── Login ─────────────────────────────────────────────────────────────
        composable("login") {
            LoginScreen(
                onNavigateTo = { route ->
                    navController.navigate(route) {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        // ── Customer (shell with bottom nav: Menu | Orders | Profile) ─────────
        composable("customer") {
            CustomerShell(onLogout = onLogout)
        }

        // ── Kitchen (standalone full-screen) ──────────────────────────────────
        composable("kitchen") {
            KitchenScreen(onLogout = onLogout)
        }

        // ── POS (standalone full-screen) ──────────────────────────────────────
        composable("pos") {
            PosScreen(onLogout = onLogout)
        }

        // ── Admin (shell with bottom nav: Dashboard | Orders) ─────────────────
        composable("admin") {
            AdminShell(onLogout = onLogout)
        }
    }
}
