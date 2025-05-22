package com.example.taller3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.taller3.ui.screens.LoginScreen
import com.example.taller3.ui.screens.RegisterScreen
import com.example.taller3.ui.screens.HomeScreen
import com.example.taller3.ui.screens.EditProfileScreen
import com.example.taller3.viewmodel.AuthViewModel
import com.example.taller3.ui.theme.taller3Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            taller3Theme {
                Surface(color = colorScheme.background) {
                    val navController: NavHostController = rememberNavController()
                    val authViewModel = remember { AuthViewModel() }

                    NavHost(
                        navController = navController,
                        startDestination = if (authViewModel.getCurrentUser() != null) "home" else "login"
                    ) {
                        composable("login") {
                            LoginScreen(
                                viewModel = authViewModel,
                                onLoginSuccess = { navController.navigate("home") },
                                onNavigateToRegister = { navController.navigate("register") }
                            )
                        }

                        composable("register") {
                            RegisterScreen(
                                viewModel = authViewModel,
                                onRegisterSuccess = { navController.popBackStack("login", inclusive = false) }
                            )
                        }

                        composable("home") {
                            HomeScreen(
                                viewModel = authViewModel,
                                onLogout = {
                                    authViewModel.logout()
                                    navController.navigate("login") {
                                        popUpTo("home") { inclusive = true }
                                    }
                                },
                                onEditProfile = {
                                    navController.navigate("editProfile")
                                }
                            )
                        }

                        composable("editProfile") {
                            EditProfileScreen(
                                onProfileUpdated = {
                                    navController.popBackStack()
                                }
                            )
                        }

                    }
                }
            }
        }
    }
}
