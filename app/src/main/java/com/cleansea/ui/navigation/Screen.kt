package com.cleansea.ui.navigation

sealed class Screen(val route: String) {
    object Auth : Screen("auth_screen")
    object Map : Screen("map_screen")
    // Добавляйте другие экраны по мере необходимости
    object Profile : Screen("profile_screen")
    object Settings : Screen("settings_screen")

    fun withArgs(vararg args: String): String {
        return buildString {
            append(route)
            args.forEach { arg ->
                append("/$arg")
            }
        }
    }

    // Пример использования для передачи аргументов
    // object Detail : Screen("detail_screen/{itemId}") {
    //    fun createRoute(itemId: String) = "detail_screen/$itemId"
    // }
}