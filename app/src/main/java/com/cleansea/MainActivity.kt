package com.cleansea

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cleansea.ui.navigation.Screen
import com.cleansea.ui.screens.AddPointScreen
import com.cleansea.ui.screens.AuthScreen
import com.cleansea.ui.screens.MapScreen
import com.cleansea.ui.theme.CleanSeaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CleanSeaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val viewModel: MainViewModel = viewModel() // Создаем одну ViewModel для всего графа

    // Определяем стартовый экран. Если пользователь уже авторизован (состояние могло сохраниться),
    // то сразу на карту, иначе - на экран входа.
    val startDestination = if (viewModel.isAuthenticated.value) Screen.Map.route else Screen.Auth.route

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Auth.route) {
            AuthScreen(navController = navController, viewModel = viewModel)
        }
        composable(Screen.Map.route) {
            MapScreen(navController = navController, viewModel = viewModel)
        }
        composable(Screen.AddPoint.route) {
            AddPointScreen(navController = navController, viewModel = viewModel)
        }
    }
}