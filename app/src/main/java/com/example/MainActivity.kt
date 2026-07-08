package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.LayoutDirection
import com.example.ui.screens.*
import com.example.ui.theme.WalkEarnTheme
import com.example.ui.viewmodel.WalkEarnViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: WalkEarnViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val currentLanguage by viewModel.currentLanguage.collectAsState()
            val currentScreen by viewModel.currentScreen.collectAsState()

            // Dynamic RTL / LTR layout switching
            val layoutDirection = if (currentLanguage == "ar") {
                LayoutDirection.Rtl
            } else {
                LayoutDirection.Ltr
            }

            CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                WalkEarnTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        when (currentScreen) {
                            "login" -> {
                                LoginScreen(viewModel = viewModel)
                            }
                            "admin_panel" -> {
                                Box(modifier = Modifier.safeDrawingPadding()) {
                                    AdminPanelScreen(viewModel = viewModel)
                                }
                            }
                            else -> {
                                // Master Screen Layout for Dashboard, Wallet, Tasks, Shop, Settings
                                Scaffold(
                                    modifier = Modifier.fillMaxSize(),
                                    bottomBar = {
                                        NavigationBar(
                                            modifier = Modifier.testTag("bottom_nav_bar")
                                        ) {
                                            NavigationBarItem(
                                                selected = currentScreen == "dashboard",
                                                onClick = { viewModel.navigateTo("dashboard") },
                                                icon = { Icon(Icons.Default.DirectionsRun, contentDescription = "Walk") },
                                                label = { Text(viewModel.getLabel("dashboard")) },
                                                modifier = Modifier.testTag("nav_dashboard")
                                            )
                                            NavigationBarItem(
                                                selected = currentScreen == "wallet",
                                                onClick = { viewModel.navigateTo("wallet") },
                                                icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Wallet") },
                                                label = { Text(viewModel.getLabel("wallet")) },
                                                modifier = Modifier.testTag("nav_wallet")
                                            )
                                            NavigationBarItem(
                                                selected = currentScreen == "tasks",
                                                onClick = { viewModel.navigateTo("tasks") },
                                                icon = { Icon(Icons.Default.AddTask, contentDescription = "Tasks") },
                                                label = { Text(viewModel.getLabel("tasks")) },
                                                modifier = Modifier.testTag("nav_tasks")
                                            )
                                            NavigationBarItem(
                                                selected = currentScreen == "shop",
                                                onClick = { viewModel.navigateTo("shop") },
                                                icon = { Icon(Icons.Default.ShoppingBag, contentDescription = "Shop") },
                                                label = { Text(viewModel.getLabel("shop")) },
                                                modifier = Modifier.testTag("nav_shop")
                                            )
                                            NavigationBarItem(
                                                selected = currentScreen == "settings",
                                                onClick = { viewModel.navigateTo("settings") },
                                                icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                                                label = { Text(viewModel.getLabel("settings")) },
                                                modifier = Modifier.testTag("nav_settings")
                                            )
                                        }
                                    }
                                ) { innerPadding ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(innerPadding)
                                    ) {
                                        when (currentScreen) {
                                            "dashboard" -> DashboardScreen(viewModel = viewModel)
                                            "wallet" -> WalletScreen(viewModel = viewModel)
                                            "tasks" -> TasksScreen(viewModel = viewModel)
                                            "shop" -> ShopScreen(viewModel = viewModel)
                                            "settings" -> SettingsScreen(viewModel = viewModel)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
