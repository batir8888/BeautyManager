package ru.batir8888.beautymanager.navigation

sealed class Window(val route: String, val title: String) {
    data object Clients : Window("clients", "Клиенты")
    data object Records : Window("records", "Записи")
    data object Reports : Window("reports", "Отчёты")
    data object Analytics : Window("analytics", "Аналитика")
    data object ClientCard : Window("client_card/{clientId}", "Карта клиента") {
        fun createRoute(clientId: Int) = "client_card/$clientId"
    }
}