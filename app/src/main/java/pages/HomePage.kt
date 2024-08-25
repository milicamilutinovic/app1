package pages

import android.content.Context
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.app1.AuthState
import com.example.app1.AuthViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.gson.Gson
import com.google.android.gms.maps.model.MapStyleOptions
import com.example.app1.MarkerViewModel


@Composable
fun HomePage(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current
    val authState by authViewModel.authState.observeAsState()
    val homeViewModel: MarkerViewModel = viewModel(
        factory = HomeViewModelFactory(context)
    )

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

    // Handle new markers
    LaunchedEffect(Unit) {
        navController.currentBackStackEntry?.savedStateHandle?.getLiveData<Pair<LatLng, String>>("newMarker")
            ?.observeForever { newMarker ->
                newMarker?.let {
                    homeViewModel.addMarker(it.first.latitude, it.first.longitude, it.second) // Add marker to ViewModel
                }
            }
    }

    val markers by homeViewModel.markers.collectAsState()

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
            Log.e("HomePage", "Location permission not granted")
        }
    }

    LaunchedEffect(authState) {
        if (authState is AuthState.Unauthenticated) {
            navController.navigate("login")
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

    // State to manage marker selection and dialog visibility
    var selectedMarker by remember { mutableStateOf<LatLng?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    val markerName = remember { mutableStateOf(TextFieldValue("")) }

    // Dropdown menu state
    var expanded by remember { mutableStateOf(false) }

    // Search bar state
    val searchQuery = remember { mutableStateOf(TextFieldValue("")) }

    // Filter buttons state
    var selectedColor by remember { mutableStateOf<Color?>(null) }

    // State for LandmarkFilterDialog
    var showFilterDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        MarkerNameDialog(
            markerName = markerName,
            onDismiss = { showDialog = false },
            onConfirm = {
                selectedMarker?.let {
                    homeViewModel.addMarker(it.latitude, it.longitude, markerName.value.text)
                }
                showDialog = false
            }
        )
    }

    if (showFilterDialog) {
        LandmarkFilterDialog(
            onDismiss = { showFilterDialog = false }
        )
    }

    var selectedMapStyle by remember { mutableStateOf<MapStyleOptions?>(null) }
    val recenterMap = {
        currentLocation.value?.let {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(it, 15f)
        }
    }
    //DODATO
   // var markerViewModel:MarkerViewModel= viewModel()
    val filteredMarkers by homeViewModel.filteredMarkers.observeAsState(emptyList())
    val isFilterApplied by homeViewModel.isFilterApplied.observeAsState(false) // Da li je filter primenjen
    var isFilterButtonPressed by remember { mutableStateOf(false) }
    var isMarkerButtonPressed by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        val markersToDisplay = if (isFilterApplied) {
            filteredMarkers // Prikazujemo filtrirane markere
        } else {
            markers // Prikazujemo sve markere
        }
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            onMapClick = { latLng ->
                if (!isFilterButtonPressed) {
                    selectedMarker = latLng
                    showDialog = true
                    isMarkerButtonPressed=true
                } // Show dialog to name the marker
            }
        ) {
            currentLocation.value?.let {
                Marker(
                    state = MarkerState(position = it),
                    title = "My Location",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE) // Optional: Different color for marker
                )
            }

            markersToDisplay.filter { it.eventName.contains(searchQuery.value.text, ignoreCase = true) }
                .forEach { marker ->
                    Marker(
                        state = MarkerState(position = LatLng(marker.location.latitude, marker.location.longitude)),
                        title = marker.eventName,
                        icon = if (selectedColor == null || selectedColor == Color.Red) {
                            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                        } else {
                            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
                        } ,
                        onClick = {
                            val markerJson = Gson().toJson(marker)
                            navController.currentBackStackEntry?.savedStateHandle?.set("markerData", markerJson)
                            navController.navigate("LandmarkDetailsPage")
                            true
                        }
                    )
                }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Search and Options Row
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
            Button(
                onClick = {
                    homeViewModel.resetFilter() // Resetuje filter
                },
                modifier = Modifier
                    .fillMaxWidth() // Puni širinu
                    .padding(vertical = 8.dp), // Razmak oko dugmeta
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF75553) // Boja pozadine dugmeta
                )
            ) {
                Text(
                    text = "Reset Filter",
                    fontSize = 16.sp,
                    color = Color.White
                )
            }

            // Filter and Add Buttons Row
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .wrapContentSize(Alignment.BottomCenter),
                horizontalArrangement = Arrangement.Center
            ) {
                TextButton(
                    onClick = {
                        showFilterDialog = true // Show filter dialog
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red,
                        contentColor = Color.Black
                    )
                ) {
                    Text("Filter")
                }
                Spacer(modifier = Modifier.width(16.dp)) // Add space between the buttons

                TextButton(
                    onClick = {
                        currentLocation.value?.let { location ->
                            navController.currentBackStackEntry?.savedStateHandle?.set("location", location)
                            navController.navigate("add_landmark")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red,
                        contentColor = Color.Black
                    )
                ) {
                    Text("Add")
                }
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
class HomeViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MarkerViewModel::class.java)) {
            return MarkerViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
