package com.example.myabsen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myabsen.data.di.Injection
import com.example.myabsen.data.local.pref.UserModel
import com.example.myabsen.data.local.pref.UserPreference
import com.example.myabsen.data.local.pref.dataStore
import com.example.myabsen.ui.screen.home.HomeScreen
import com.example.myabsen.ui.navigation.Screen
import com.example.myabsen.ui.screen.login.LoginScreen
import com.example.myabsen.ui.screen.login.LoginViewModel
import com.example.myabsen.ui.screen.register.RegisterScreen
import com.example.myabsen.ui.screen.register.RegisterViewModel
import com.example.myabsen.ui.screen.splash.SplashScreen
import com.example.myabsen.ui.screen.welcome.WelcomeScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

@Composable
fun MyAbsenApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val userRepository = Injection.provideRepository()
    val context = LocalContext.current
    val userPreference = remember { UserPreference.getInstance(context.dataStore) }
    var user by remember { mutableStateOf<UserModel?>(null) }

    LaunchedEffect(userPreference) {
        val userFlow = userPreference.getSession().first()
        user = userFlow
        delay(2000)
        navigateToNextScreen(navController, user)
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = modifier.fillMaxSize()
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                modifier = Modifier.fillMaxSize(),
                onTimeout = {
                    navigateToNextScreen(navController, user)
                }
            )
        }
        composable(Screen.Welcome.route) {
            WelcomeScreen(
                navigateToRegister = {
                    navController.navigate(Screen.Register.route) {
                        popUpTo(Screen.Welcome.route) {
                            saveState = true
                        }
                        restoreState = true
                        launchSingleTop = true
                    }
                },
                navigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Welcome.route) {
                            saveState = true
                        }
                        restoreState = true
                        launchSingleTop = true
                    }
                },
            )
        }
        composable(Screen.Main.route) {
            MainScreen()
        }
        composable(Screen.Login.route) {
            val loginViewModel = LoginViewModel(userRepository)
            LoginScreen(
                onBackClick = {
                    navController.navigateUp()
                },
                onRegisterClick = {
                    navController.navigate(Screen.Register.route) {
                        popUpTo(Screen.Welcome.route) {
                            saveState = true
                        }
                        restoreState = true
                        launchSingleTop = true
                    }
                },
                onButtonLoginClick = {
                    navController.navigate(Screen.Main.route)
                },
                viewModel = loginViewModel
            )
        }
        composable(Screen.Register.route) {
            val registerViewModel = RegisterViewModel(userRepository)
            RegisterScreen(
                onBackClick = {
                    navController.navigateUp()
                },
                onButtonClick = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Welcome.route) {
                            saveState = true
                        }
                        restoreState = true
                        launchSingleTop = true
                    }
                },
                viewModel = registerViewModel
            )
        }
    }
}

private fun navigateToNextScreen(navController: NavHostController, user: UserModel?) {
    val destination = when {
        user == null -> Screen.Welcome.route
        user.isLogin -> Screen.Main.route
        else -> Screen.Welcome.route
    }

    navController.navigate(destination) {
        popUpTo(Screen.Splash.route) { inclusive = true }
    }
}