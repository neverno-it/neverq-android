package com.neverno.neverq

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.navigation.compose.rememberNavController
import com.neverno.neverq.core.data.SyncWorker
import com.neverno.neverq.core.data.TokenManager
import com.neverno.neverq.navigation.NeverQNavGraph
import com.neverno.neverq.ui.theme.NeverQTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NeverQTheme {
                val navController = rememberNavController()
                var startDestination by remember { mutableStateOf<String?>(null) }
                val scope = rememberCoroutineScope()

                LaunchedEffect(Unit) {
                    scope.launch {
                        val isLoggedIn = tokenManager.isLoggedIn()
                        val userType = tokenManager.getUserTypeNow()
                        val role = tokenManager.getUserRoleNow()

                        startDestination = if (!isLoggedIn) {
                            "login"
                        } else {
                            // Start offline sync for staff
                            if (userType == "staff" && role != null) {
                                SyncWorker.schedule(applicationContext, userType, role)
                            }
                            when {
                                userType == "customer" -> "customer_home"
                                role == "cafeman" -> "kitchen"
                                role == "pos" -> "pos"
                                else -> "admin_dashboard"
                            }
                        }
                    }
                }

                startDestination?.let { start ->
                    NeverQNavGraph(navController = navController, startDestination = start)
                }
            }
        }
    }
}
