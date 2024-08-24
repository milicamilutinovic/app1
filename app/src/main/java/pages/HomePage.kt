package pages

import android.annotation.SuppressLint
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
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.google.android.gms.maps.model.MapStyleOptions
import androidx.compose.runtime.*
import com.example.app1.R

// Data class for markers
data class MarkerData(val latitude: Double, val longitude: Double, val name: String)

private const val PREFS_NAME = "app_prefs"
private const val MARKERS_KEY = "markers_key"

// Save markers to SharedPreferences
fun saveMarkers(context: Context, markers: List<MarkerData>) {
    val gson = Gson()
    val json = gson.toJson(markers)
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
        .putString(MARKERS_KEY, json)
        .apply()
}

// Load markers from SharedPreferences
fun loadMarkers(context: Context): List<MarkerData> {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val gson = Gson()
    val json = prefs.getString(MARKERS_KEY, "[]") ?: "[]"
    val type = object : TypeToken<List<MarkerData>>() {}.type
    return gson.fromJson(json, type)
}

class HomeViewModel(private val context: Context) : ViewModel() {
    private val _markers = MutableStateFlow<List<MarkerData>>(emptyList())
    val markers: StateFlow<List<MarkerData>> = _markers

    init {
        loadMarkers()
    }

    fun addMarker(latitude: Double, longitude: Double, name: String) {
        val newMarker = MarkerData(latitude, longitude, name)
        val updatedMarkers = _markers.value + newMarker
        _markers.value = updatedMarkers
        saveMarkers(context, updatedMarkers)
    }

    private fun loadMarkers() {
        _markers.value = loadMarkers(context)
    }

    fun clearMarkers() {
        _markers.value = emptyList()
        saveMarkers(context, _markers.value)
    }
}

@SuppressLint("MissingPermission")
@Composable
fun HomePage(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current
    val authState by authViewModel.authState.observeAsState()
    val homeViewModel: HomeViewModel = viewModel(
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

    val markers by homeViewModel.markers.collectAsState(initial = emptyList())

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
    var selectedMapStyle by remember { mutableStateOf<MapStyleOptions?>(null) }
    //selectedMapStyle = MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style)
    // Recenter Button State
    val recenterMap = {
        currentLocation.value?.let {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(it, 15f)
        }
    }
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            //mapStyleOptions = selectedMapStyle,
            onMapClick = { latLng ->
                selectedMarker = latLng
                showDialog = true // Show dialog to name the marker
            }
        ) {
            currentLocation.value?.let {
//                Circle(
//                    center = it,
//                    radius = 100.0, // Radius in meters
//                    strokeColor = Color.Blue,
//                    strokeWidth = 2f,
//                    fillColor = Color.Blue.copy(alpha = 0.3f)
//                )
                Marker(
                    state = MarkerState(position = it),
                    title = "My Location",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE) // Optional: Different color for marker
                )
            }

            markers.filter { it.name.contains(searchQuery.value.text, ignoreCase = true) }
                .forEach { marker ->
                    Marker(
                        state = MarkerState(position = LatLng(marker.latitude, marker.longitude)),
                        title = marker.name,
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
                        text = { Text("All Users", color = Color.Black)
                        },
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

            // Filter and Add Buttons Row
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
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
