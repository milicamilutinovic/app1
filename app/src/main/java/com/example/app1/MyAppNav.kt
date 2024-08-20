package com.example.app1

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import pages.HomePage
import pages.LocationServicePage
import pages.LoginPage
import pages.RegisterPage
import pages.UserProfilePage

import pages.UsersPage

@Composable
fun MyAppNavigation(modifier: Modifier = Modifier, authViewModel: AuthViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login", builder = {
        composable("login") {
            LoginPage(modifier, navController, authViewModel)
        }
        composable("signup") {
            RegisterPage(modifier, navController, authViewModel)
        }
        composable("home") {
            HomePage(modifier, navController, authViewModel)
        }
        composable("user_profile") {
            UserProfilePage(modifier,navController,authViewModel)
        }
        composable("all_users") {
            UsersPage(navController=navController)
        }
        composable("settings") {
            LocationServicePage(navController=navController)
        }
    })
}
