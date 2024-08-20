package pages

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.app1.AuthState
import com.example.app1.AuthViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@SuppressLint("MissingPermission")
@Composable
fun HomePage(modifier: Modifier = Modifier, navController: NavController, authViewModel: AuthViewModel) {

    val context = LocalContext.current
    val authState = authViewModel.authState.observeAsState()

    // Manage current location state
    val currentLocation = remember { mutableStateOf<LatLng?>(null) }

    // Initialize FusedLocationProviderClient
    val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    // Request location updates
    val locationRequest = com.google.android.gms.location.LocationRequest.create().apply {
        interval = 10000 // Update interval in milliseconds
        fastestInterval = 5000 // Fastest update interval in milliseconds
        priority = com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    // Start location updates
    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, object : com.google.android.gms.location.LocationCallback() {
                override fun onLocationResult(locationResult: com.google.android.gms.location.LocationResult) {
                    super.onLocationResult(locationResult)
                    locationResult.locations.forEach { location ->
                        Log.d("HomePage", "Updated Location: ${location.latitude}, ${location.longitude}")
                        currentLocation.value = LatLng(location.latitude, location.longitude)
                    }
                }
            }, null)
        } else {
            // Handle the case where permission is not granted
            println("Location permission not granted")
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

    // State to store all added markers
    val markers = remember { mutableStateListOf<Pair<LatLng, String>>() }
    var selectedMarker by remember { mutableStateOf<LatLng?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    val markerName = remember { mutableStateOf(TextFieldValue("")) }

    // Dropdown menu state
    var expanded by remember { mutableStateOf(false) }

    // Search bar state
    val searchQuery = remember { mutableStateOf(TextFieldValue("")) }

    // Filter buttons state
    var selectedColor by remember { mutableStateOf<Color?>(null) }

    if (showDialog) {
        // Show a dialog to input marker name
        MarkerNameDialog(
            markerName = markerName,
            onDismiss = { showDialog = false },
            onConfirm = {
                markers.add(Pair(selectedMarker!!, markerName.value.text))
                showDialog = false
            }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Add Google Map
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            onMapClick = { latLng ->
                selectedMarker = latLng
                showDialog = true // Show dialog to name the marker
            }
        ) {
            currentLocation.value?.let {
                Circle(
                    center = it,
                    radius = 100.0, // Radius in meters
                    strokeColor = Color.Blue,
                    strokeWidth = 2f,
                    fillColor = Color.Blue.copy(alpha = 0.3f)
                )
                Marker(
                    state = MarkerState(position = it),
                    title = "My Location",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE) // Optional: Different color for marker
                )
            }
            // Filter and add markers from the list based on search query and color filter
            markers.filter { it.second.contains(searchQuery.value.text, ignoreCase = true) }
                .forEach { (location, name) ->
                    Marker(
                        state = MarkerState(position = location),
                        title = name,
                        onInfoWindowClick = {
                            markers.remove(Pair(location, name)) // Remove the selected marker
                        },
                        icon = if (selectedColor == null || selectedColor == Color.Red) {
                            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                        } else {
                            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE) // Replace with your "Arno" color
                        }
                    )
                }
        }

        // Dropdown menu and search bar on top of the map
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentSize(Alignment.TopStart),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = searchQuery.value,
                    onValueChange = { newValue -> searchQuery.value = newValue },
                    placeholder = { Text("Search", color = Color.Red) },
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { expanded = true }) {
                    Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More options", tint = Color.Red)
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(Color.Red) // Background color of dropdown menu
                ) {
                    DropdownMenuItem(
                        text = { Text("User Profile", color = Color.Black) },
                        onClick = {
                            expanded = false
                            navController.navigate("user_profile")
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("All Users", color = Color.Black) },
                        onClick = {
                            expanded = false
                            navController.navigate("all_users")
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Settings", color = Color.Black) },
                        onClick = {
                            expanded = false
                            navController.navigate("settings")
                        }
                    )
                }
            }
        }

        // Filter buttons at the bottom of the map
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .wrapContentSize(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.Center
        ) {
            TextButton(
                onClick = { selectedColor = Color.Blue },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red,
                    contentColor = Color.Black
                )
            ) {
                Text("Filter")
            }

        }
    }
}

@Composable
fun MarkerNameDialog(
    markerName: MutableState<TextFieldValue>,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Marker Name", color = Color.Red) },
        text = {
            TextField(
                value = markerName.value,
                onValueChange = { newText -> markerName.value = newText },
                placeholder = { Text("Enter marker name", color = Color.Gray) }
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("OK", color = Color.Red)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        }
    )
}
