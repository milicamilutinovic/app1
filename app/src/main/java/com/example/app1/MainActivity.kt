package com.example.app1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.app1.nav.MyAppNavigation
import com.example.app1.ui.theme.App1Theme
import com.example.app1.view.AuthViewModel
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory

class MainActivity : ComponentActivity() {

    private lateinit var permission: AppPermissions

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Dozvola je odobrena
                println("Location permission granted")
            } else {
                // Dozvola je odbijena
                println("Location permission denied")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        //dodala
        FirebaseApp.initializeApp(this)
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        val provider = PlayIntegrityAppCheckProviderFactory.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(provider)


        // Inicijalizacija AuthViewModel-a
        val authViewModel: AuthViewModel by viewModels()

        // Inicijalizacija AppPermissions
        permission = AppPermissions()

        // Provera i traženje dozvola za lokaciju
        if (permission.isLocationOk(this)) {
            println("Location permission allowed")
        } else {
            requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
            println("Location permission denied")
        }

        // Postavljanje sadržaja sa Compose temom
        setContent {
            App1Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MyAppNavigation(modifier = Modifier.padding(innerPadding), authViewModel = authViewModel)
                }
            }
        }
    }
}