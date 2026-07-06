package com.example.fixedexpeneses.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.fixedexpeneses.navigation.AppRoute
import com.example.fixedexpeneses.navigation.TopLevelDestination
import com.example.fixedexpeneses.ui.theme.FixedExpenesesTheme

@PreviewScreenSizes
@Composable
fun FixedExpenesesApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            NavigationBar {
                TopLevelDestination.entries.forEach { destination ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                painterResource(destination.icon),
                                contentDescription = destination.label
                            )
                        },
                        label = { Text(destination.label) },
                        selected = currentDestination.isTopLevelDestinationInHierarchy(destination),
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppRoute.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(AppRoute.Home.route) {
                PlaceholderScreen("Home")
            }

            composable(AppRoute.RecurringMonthlyTransactions.route) {
                PlaceholderScreen("Contas fixas")
            }

            composable(AppRoute.CreateRecurringMonthlyTransaction.route) {
                PlaceholderScreen("Criar conta fixa")
            }

            composable(AppRoute.RecurringMonthlyTransactionDetail.route) {
                PlaceholderScreen("Detalhe da conta fixa")
            }

            composable(AppRoute.EditRecurringMonthlyTransaction.route) {
                PlaceholderScreen("Editar conta fixa")
            }

            composable(AppRoute.InstallmentTransactions.route) {
                PlaceholderScreen("Parceladas")
            }
        }
    }
}

@Composable
fun PlaceholderScreen(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        modifier = modifier
    )
}

private fun NavDestination?.isTopLevelDestinationInHierarchy(
    destination: TopLevelDestination
): Boolean =
    this?.hierarchy?.any { it.route == destination.route } == true

@Preview(showBackground = true)
@Composable
fun PlaceholderScreenPreview() {
    FixedExpenesesTheme {
        PlaceholderScreen("Home")
    }
}
