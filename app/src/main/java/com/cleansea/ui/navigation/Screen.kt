package com.cleansea.ui.navigation

sealed class Screen(val route: String) {
    object Auth : Screen("auth_screen")
    object Map : Screen("map_screen")
    object AddPoint : Screen("add_point_screen")
}