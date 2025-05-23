package ru.batir8888.beautymanager.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun BottomNavigationBar(navController: NavController) {

    // порядок отображения
    val items = listOf(
        Window.Clients,
        Window.Records,
        Window.Reports,
        Window.Analytics
    )

    val selected = MaterialTheme.colorScheme.onBackground
    val unSelected = MaterialTheme.colorScheme.primary.copy(alpha = .5f)

    NavigationBar(
        containerColor = Color.Transparent
    ) {
        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = backStackEntry?.destination?.route

        items.forEach { window ->
            NavigationBarItem(
                selected = currentRoute == window.route,
                onClick = {
                    if (currentRoute != window.route) {
                        navController.navigate(window.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon  = { Icon(getIcon(window), contentDescription = window.title, tint = MaterialTheme.colorScheme.onBackground) },
                label = { Text(window.title) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = selected,
                    selectedTextColor = selected,
                    unselectedIconColor = unSelected,
                    unselectedTextColor = unSelected,
                    indicatorColor = selected.copy(alpha = .15f)
                )
            )
        }
    }
}

private fun getIcon(screen: Window): ImageVector = when (screen) {
    Window.Clients      -> Icons.Default.AccountCircle
    Window.Records      -> Icons.Default.DateRange
    Window.Reports -> Icons.Default.MonetizationOn
    Window.Analytics    -> Icons.Default.BarChart
    else                -> Icons.Default.AccountCircle   // для AddEditClient не нужен в bottom bar
}
