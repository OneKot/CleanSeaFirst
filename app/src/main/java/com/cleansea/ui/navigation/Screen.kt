package com.cleansea.ui.navigation

sealed class Screen(val route: String, val title: String) { // Добавим title для удобства
    object Auth : Screen("auth_screen", "Авторизация")
    object Map : Screen("map_screen", "Карта")
    object AddPoint : Screen("add_point_screen", "Добавить точку")
    object Statistics : Screen("statistics_screen", "Статистика") // <-- НОВЫЙ ЭКРАН
}