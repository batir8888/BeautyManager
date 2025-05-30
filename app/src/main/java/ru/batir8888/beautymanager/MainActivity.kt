package ru.batir8888.beautymanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ru.batir8888.beautymanager.navigation.BottomNavigationBar
import ru.batir8888.beautymanager.navigation.Window
import ru.batir8888.beautymanager.ui.*
import ru.batir8888.beautymanager.ui.theme.BeautyManagerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BeautyManagerTheme {
                val isDark = isSystemInDarkTheme()

                SideEffect {
                    // Цвета для beauty-темы
                    val statusBarColor = if (isDark) {
                        0xFF1F1B2E.toInt()  // Темно-фиолетовый для темной темы
                    } else {
                        0xFFFDF2F8.toInt()  // Мягкий розовый для светлой темы
                    }

                    val navigationBarColor = if (isDark) {
                        0xFF2A1F3D.toInt()  // Глубокий баклажан для темной темы
                    } else {
                        0xFFFFFBFE.toInt()  // Теплый белый для светлой темы
                    }

                    window.statusBarColor = statusBarColor
                    window.navigationBarColor = navigationBarColor

                    // Настройка цвета иконок статус-бара и навигации
                    WindowInsetsControllerCompat(window, window.decorView).apply {
                        isAppearanceLightStatusBars = !isDark      // Темные иконки в светлой теме
                        isAppearanceLightNavigationBars = !isDark  // Темные иконки в светлой теме
                    }
                }

                MainScreen()
            }
        }
    }
}

@Composable
private fun MainScreen() {

    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = Window.Clients.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Window.Clients.route) {
                ClientsMenuScreen(navController)
            }

            composable(
                Window.ClientCard.route,
                arguments = listOf(navArgument("clientId") {
                    type = NavType.IntType
                    defaultValue = -1
                })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("clientId") ?: -1
                ClientCardScreen(clientId = id, navController = navController)
            }

            // --- ЗАПИСИ ---
            composable(Window.Records.route)      { ScheduleScreen() }

            // --- ПРОЧИЕ ТРАНЗАКЦИИ ---
            composable(Window.Reports.route) { ReportsScreen() }

            // --- АНАЛИТИКА ---
            composable(Window.Analytics.route)    { AnalyticsScreen() }
        }
    }
}
