package com.cleansea.ui.navigation

import androidx.annotation.StringRes
import com.cleansea.R

sealed class Screen(val route: String, @StringRes val titleResId: Int) {
    object Auth : Screen("auth_screen", R.string.screen_title_auth)
    object Map : Screen("map_screen", R.string.screen_title_map)
    object AddPoint : Screen("add_point_screen", R.string.screen_title_add_point)
    object Statistics : Screen("statistics_screen", R.string.screen_title_statistics)
    object Settings : Screen("settings_screen", R.string.screen_title_settings)
}