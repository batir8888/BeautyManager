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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun BottomNavigationBar(navController: NavController) {

    val items = listOf(
        Window.Clients,
        Window.Records,
        Window.Reports,
        Window.Analytics
    )

    val selectedColor = MaterialTheme.colorScheme.primary          // Глубокий розовый/розовое золото
    val unSelectedColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)  // Приглушенный серый
    val indicatorColor = MaterialTheme.colorScheme.primaryContainer  // Мягкий розовый контейнер

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f), // Полупрозрачная поверхность
        tonalElevation = 8.dp
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
                icon = {
                    Icon(
                        imageVector = getIcon(window),
                        contentDescription = window.title
                    )
                },
                label = {
                    Text(
                        text = window.title,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = selectedColor,
                    selectedTextColor = selectedColor,
                    unselectedIconColor = unSelectedColor,
                    unselectedTextColor = unSelectedColor,
                    indicatorColor = indicatorColor
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
