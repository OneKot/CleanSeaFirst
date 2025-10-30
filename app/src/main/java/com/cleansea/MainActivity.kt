package com.cleansea

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.cleansea.ui.navigation.Screen
import com.cleansea.ui.screens.AddPointScreen
import com.cleansea.ui.screens.AuthScreen
import com.cleansea.ui.screens.MapScreen
import com.cleansea.ui.screens.StatisticsScreen
import com.cleansea.ui.theme.CleanSeaTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CleanSeaTheme {
                AppNavigation()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val viewModel: MainViewModel = viewModel()
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Показываем боковое меню только если пользователь авторизован
    if (viewModel.isAuthenticated.value) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                AppDrawerContent(navController = navController, currentRoute = currentRoute) {
                    scope.launch { drawerState.close() }
                }

            },
            gesturesEnabled = false
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            val title = when (currentRoute) {
                                Screen.Map.route -> Screen.Map.title
                                Screen.Statistics.route -> Screen.Statistics.title
                                else -> "Чистый Каспий"
                            }
                            Text(title)
                        },
                        navigationIcon = {
                            IconButton(onClick = {
                                scope.launch { drawerState.open() }
                            }) {
                                Icon(Icons.Filled.Menu, contentDescription = "Меню")
                            }
                        }
                    )
                }
            ) { paddingValues ->
                AppNavHost(
                    navController = navController,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    } else {
        // Если не авторизован - показываем только NavHost с экраном входа
        AppNavHost(navController = navController)
    }
}

@Composable
fun AppNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    val viewModel: MainViewModel = viewModel()
    val startDestination = if (viewModel.isAuthenticated.value) Screen.Map.route else Screen.Auth.route

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Auth.route) {
            AuthScreen(navController = navController, viewModel = viewModel)
        }
        composable(Screen.Map.route) {
            MapScreen(navController = navController, viewModel = viewModel)
        }
        composable(Screen.AddPoint.route) {
            // У экрана добавления точки не будет бокового меню, т.к. он не в основной навигации
            AddPointScreen(navController = navController, viewModel = viewModel)
        }
        composable(Screen.Statistics.route) {
            StatisticsScreen(viewModel = viewModel)
        }
    }
}

@Composable
fun AppDrawerContent(
    navController: NavHostController,
    currentRoute: String?,
    onCloseDrawer: () -> Unit
) {
    ModalDrawerSheet {
        Text("Меню", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleLarge)
        Divider()
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Map, contentDescription = "Карта") },
            label = { Text(Screen.Map.title) },
            selected = currentRoute == Screen.Map.route,
            onClick = {
                navController.navigate(Screen.Map.route) { popUpTo(Screen.Map.route) { inclusive = true } }
                onCloseDrawer()
            }
        )
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.BarChart, contentDescription = "Статистика") },
            label = { Text(Screen.Statistics.title) },
            selected = currentRoute == Screen.Statistics.route,
            onClick = {
                navController.navigate(Screen.Statistics.route) { popUpTo(Screen.Map.route) } // Возврат на карту по кнопке "назад"
                onCloseDrawer()
            }
        )
    }
}