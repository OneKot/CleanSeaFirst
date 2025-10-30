package com.cleansea.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.cleansea.MainViewModel
import com.cleansea.ui.components.AppLogo
import com.cleansea.ui.navigation.Screen

@Composable
fun AuthScreen(navController: NavController, viewModel: MainViewModel = viewModel()) {
    var showLogin by remember { mutableStateOf(true) } // Переключатель между входом и регистрацией

    LaunchedEffect(viewModel.isAuthenticated.value) {
        if (viewModel.isAuthenticated.value) {
            navController.navigate(Screen.Map.route) {
                popUpTo(Screen.Auth.route) { inclusive = true } // Удаляем из бэкстека
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AppLogo()
        Spacer(Modifier.height(32.dp))

        if (showLogin) {
            LoginContent(
                viewModel = viewModel,
                onNavigateToRegister = { showLogin = false }
            )
        } else {
            RegisterContent(
                viewModel = viewModel,
                onNavigateToLogin = { showLogin = true }
            )
        }

        viewModel.errorMessage.value?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun LoginContent(viewModel: MainViewModel, onNavigateToRegister: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Text(text = "Вход", style = MaterialTheme.typography.headlineMedium)
    Spacer(Modifier.height(16.dp))

    OutlinedTextField(
        value = email,
        onValueChange = { email = it },
        label = { Text("Email или логин") },
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(8.dp))
    OutlinedTextField(
        value = password,
        onValueChange = { password = it },
        label = { Text("Пароль") },
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(imageVector = image, contentDescription = "Показать/скрыть пароль")
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(16.dp))
    Button(
        onClick = { viewModel.login(email, password) },
        enabled = !viewModel.isLoading.value,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Войти")
    }
    Spacer(Modifier.height(8.dp))
    TextButton(onClick = onNavigateToRegister) {
        Text("У вас нет аккаунта? Зарегистрироваться")
    }
}

@Composable
fun RegisterContent(viewModel: MainViewModel, onNavigateToLogin: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var repeatPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Text(text = "Регистрация", style = MaterialTheme.typography.headlineMedium)
    Spacer(Modifier.height(16.dp))

    OutlinedTextField(
        value = email,
        onValueChange = { email = it },
        label = { Text("Email") },
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(8.dp))
    OutlinedTextField(
        value = password,
        onValueChange = { password = it },
        label = { Text("Пароль") },
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(imageVector = image, contentDescription = "Показать/скрыть пароль")
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(8.dp))
    OutlinedTextField(
        value = repeatPassword,
        onValueChange = { repeatPassword = it },
        label = { Text("Повторите пароль") },
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(imageVector = image, contentDescription = "Показать/скрыть пароль")
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(16.dp))
    Button(
        onClick = {
            if (password == repeatPassword) {
                viewModel.register(email, password)
            } else {
                viewModel.errorMessage.value = "Пароли не совпадают"
            }
        },
        enabled = !viewModel.isLoading.value,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Зарегистрироваться")
    }
    Spacer(Modifier.height(8.dp))
    TextButton(onClick = onNavigateToLogin) {
        Text("Уже есть аккаунт? Войти")
    }
}