package pages

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.app1.AuthState
import com.example.app1.AuthViewModel
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@SuppressLint("MissingPermission")
@Composable
fun HomePage(modifier: Modifier = Modifier, navController: NavController, authViewModel: AuthViewModel) {

    val context = LocalContext.current
    val authState = authViewModel.authState.observeAsState()

    // Manage current location state
    val currentLocation = remember { mutableStateOf<LatLng?>(null) }
    val searchQuery = remember { mutableStateOf("") }

    // Initialize FusedLocationProviderClient
    val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    // Create a LocationRequest to define how location updates should be received
    val locationRequest = LocationRequest.create().apply {
        interval = 10000 // Update interval in milliseconds
        fastestInterval = 5000 // Fastest update interval in milliseconds
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    // Create a LocationCallback to handle location updates
    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            locationResult.locations.forEach { location ->
                Log.d("HomePage", "Updated Location: ${location.latitude}, ${location.longitude}")
                currentLocation.value = LatLng(location.latitude, location.longitude)
            }
        }
    }

    // Start location updates
    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        } else {
            // Handle the case where permission is not granted
            Log.d("HomePage", "Location permission not granted")
        }
    }

    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Unauthenticated -> navController.navigate("login")
            else -> Unit
        }
    }

    // Create a CameraPositionState to control the map's camera
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 15f) // Default position
    }

    // Update the camera position when the current location is available
    LaunchedEffect(currentLocation.value) {
        currentLocation.value?.let {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(it, 15f)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search Bar
        OutlinedTextField(
            value = searchQuery.value,
            onValueChange = { searchQuery.value = it },
            label = { Text("Search") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        )

        // Google Map
        GoogleMap(
            modifier = Modifier
                .weight(1f),
            cameraPositionState = cameraPositionState
        ) {
            currentLocation.value?.let {
                Marker(
                    state = MarkerState(position = it),
                    title = "Your Location"
                )
            }
        }

        // Filter Button
        TextButton(onClick = {
            // Handle filter button click
        }, modifier = Modifier.fillMaxWidth()) {
            Text(text = "Filter", fontSize = 16.sp)
        }

        // Button to open list of options
        TextButton(onClick = {
            // Navigate to options list
            navController.navigate("options_list")
        }, modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
            Text(text = "Show Options", fontSize = 16.sp)
        }

        // Sign Out Button
        TextButton(onClick = {
            authViewModel.signout()
        }, modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
            Text(text = "Sign out")
        }
    }
}
