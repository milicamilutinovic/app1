package com.example.app1

//import AddLandmarkPage
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import pages.AddLandmarkPage
import pages.AllLandmarksPage
import pages.HomePage
import pages.LandmarkDetailsPage
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
        composable("user_profile/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")
            UserProfilePage(modifier,navController,authViewModel, userId)
        }
        composable("all_users") {
            UsersPage(navController=navController)
        }
        composable("settings") {
            LocationServicePage(navController=navController)
        }
        composable("add_landmark") {
            AddLandmarkPage(navController = navController)
        }
//        composable("LandmarkDetailsPage") {
//            LandmarkDetailsPage(navController = navController)
//        }
        composable("allLandmarks"){
            AllLandmarksPage(navController =navController )
        }
        composable("landmark_details/{landmarkId}") { backStackEntry ->
            val landmarkId = backStackEntry.arguments?.getString("landmarkId")
            if (landmarkId != null) {
                LandmarkDetailsPage(landmarkId = landmarkId, navController = navController)
            }
        }
//        composable("landmark_detail/{landmarkId}") { backStackEntry ->
//            val landmarkId = backStackEntry.arguments?.getString("landmarkId")
//            if (landmarkId != null) {
//                LandmarkDetailsPage(navController,landmarkId)
//            }
//        }
//        composable("map_with_landmark/{id}") { backStackEntry ->
//            val landmarkId = backStackEntry.arguments?.getString("id") ?: return@composable
//            HomePage(navController = navController,landmarkId = landmarkId, )
//        }
    })
}
