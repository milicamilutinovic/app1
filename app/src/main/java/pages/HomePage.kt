package pages

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.rememberImagePainter
import com.example.app1.view.AuthState
import com.example.app1.view.AuthViewModel
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
import com.example.app1.view.MarkerViewModel

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

    val currentUserId =
        (authState as? AuthState.Authenticated)?.let { authViewModel.getCurrentUser()?.uid }
    val currentLocation = remember { mutableStateOf<LatLng?>(null) }
    val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    // Request location updates
    val locationRequest = com.google.android.gms.location.LocationRequest.create().apply {
        interval = 10000
        fastestInterval = 5000
        priority = com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
    }
    var showDialog by remember { mutableStateOf(false) }
    val markerName = remember { mutableStateOf(TextFieldValue("")) }
    var expanded by remember { mutableStateOf(false) }
    val searchQuery = remember { mutableStateOf(TextFieldValue("")) }
    var selectedColor by remember { mutableStateOf<Color?>(null) }
    var showFilterDialog by remember { mutableStateOf(false) }
    val matchingLandmarks = remember { mutableStateOf(emptyList<Pair<String, String>>()) }
    val keyboardController = LocalSoftwareKeyboardController.current
    var focusLocation by remember { mutableStateOf<LatLng?>(null) }

    // Handle new markers
    LaunchedEffect(Unit) {
        navController.currentBackStackEntry?.savedStateHandle?.getLiveData<Pair<LatLng, String>>("newMarker")
            ?.observeForever { newMarker ->
                newMarker?.let {
                    homeViewModel.addMarker(it.first.latitude, it.first.longitude, it.second)
                }
            }
    }

    val markers by homeViewModel.markers.collectAsState()
    LaunchedEffect(Unit) {
        navController.currentBackStackEntry?.savedStateHandle?.getLiveData<LatLng>("selectedLandmarkLocation")
            ?.observeForever { selectedLandmarkLocation ->
                selectedLandmarkLocation?.let {
                    currentLocation.value = it
                }
            }
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                object : com.google.android.gms.location.LocationCallback() {
                    override fun onLocationResult(locationResult: com.google.android.gms.location.LocationResult) {
                        super.onLocationResult(locationResult)
                        locationResult.locations.forEach { location ->
                            Log.d(
                                "HomePage",
                                "Updated Location: ${location.latitude}, ${location.longitude}"
                            )
                            currentLocation.value = LatLng(location.latitude, location.longitude)
                        }
                    }
                },
                null
            )
        } else {
            Log.e("HomePage", "Location permission not granted")
        }
    }

    LaunchedEffect(authState) {
        if (authState is AuthState.Unauthenticated) {
            navController.navigate("login")
        }
    }

    var selectedMarker by remember { mutableStateOf<LatLng?>(null) }
    // Handle landmark data if available, za ono iz tabele landmarka
    // Create a State for selected landmark location
    val selectedLandmarkLocation = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getLiveData<LatLng>("selectedLandmarkLocation")
        ?.observeAsState()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()

    // Update Camera Position if selectedLandmarkLocation is not null
    val cameraPositionState = rememberCameraPositionState() {
        position = CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 10f) // Default position
    }
    LaunchedEffect(selectedLandmarkLocation?.value) {
        selectedLandmarkLocation?.value?.let { location ->
            Log.d("HomePage", "Setting camera position to $location")
            cameraPositionState.position = CameraPosition.fromLatLngZoom(location, 15f)
        }
    }

    // State to track if we should focus on landmark or current location
    var shouldFocusOnLandmark by remember { mutableStateOf(false) }

    // Update the camera position when selectedLandmarkLocation changes
//    LaunchedEffect(selectedLandmarkLocation?.value) {
//        Log.d("HomePage", "Selected landmark location: ${selectedLandmarkLocation?.value}")
//        selectedLandmarkLocation?.value?.let { latLng ->
//            cameraPositionState.position = CameraPosition.fromLatLngZoom(latLng, 15f)
//            currentLocation.value = latLng
//            searchQuery.value = TextFieldValue("")
//            keyboardController?.hide()
//            focusLocation = latLng
//            shouldFocusOnLandmark = true
//        }
//    }


    LaunchedEffect(currentLocation.value) {
        currentLocation.value?.let { location ->
            // Only update camera position if it's different from the current one
            if (cameraPositionState.position.target != location) {
                cameraPositionState.position = CameraPosition.fromLatLngZoom(location, 15f)
            }
        }
    }


//    if (showDialog) {
//        MarkerNameDialog(
//            markerName = markerName,
//            onDismiss = { showDialog = false },
//            onConfirm = {
//                selectedMarker?.let {
//                    homeViewModel.addMarker(it.latitude, it.longitude, markerName.value.text)
//                }
//                showDialog = false
//            }
//        )
//    }


    LaunchedEffect(searchQuery.value.text) {
        matchingLandmarks.value = markers
            .filter {
                it.eventName.contains(
                    searchQuery.value.text,
                    ignoreCase = true
                ) && it.eventName.isNotBlank()
            }
            .map { it.eventName to it.mainImage }
    }

    if (showFilterDialog) {
        LandmarkFilterDialog(
            onDismiss = { showFilterDialog = false }
        )
    }

    var selectedMapStyle by remember { mutableStateOf<MapStyleOptions?>(null) }

    val navigateToLandmark = { landmarkName: String ->
        val landmark = markers.find { it.eventName == landmarkName }
        landmark?.let {
            currentLocation.value = LatLng(it.location.latitude, it.location.longitude)
            cameraPositionState.position =
                CameraPosition.fromLatLngZoom(currentLocation.value!!, 15f)
            searchQuery.value = TextFieldValue("")
            keyboardController?.hide()
            focusLocation =
                LatLng(it.location.latitude, it.location.longitude) // Set the focus location
            shouldFocusOnLandmark = true
        }
    }

    val filteredMarkers by homeViewModel.filteredMarkers.observeAsState(emptyList())
    val isFilterApplied by homeViewModel.isFilterApplied.observeAsState(false)
    var isFilterButtonPressed by remember { mutableStateOf(false) }
    var isMarkerButtonPressed by remember { mutableStateOf(false) }

    fun resetState() {
        searchQuery.value = TextFieldValue("")
        selectedMarker = null
        markerName.value = TextFieldValue("")
        isFilterButtonPressed = false
        isMarkerButtonPressed = false
        shouldFocusOnLandmark = false
    }

    val recenterMap = {
        if (focusLocation != null) {
            // Recenter the map to the focusLocation if it's set
            cameraPositionState.position = CameraPosition.fromLatLngZoom(focusLocation!!, 15f)
        } else {
            currentLocation.value?.let {
                // Otherwise, recenter to the current location
                cameraPositionState.position = CameraPosition.fromLatLngZoom(it, 15f)
            }
        }
        focusLocation = null // Reset focusLocation
        resetState()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        val markersToDisplay = if (isFilterApplied) {
            filteredMarkers
        } else {
            markers
        }

        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            onMapClick = { latLng ->
                if (!isFilterButtonPressed) {
                    selectedMarker = latLng
                    showDialog = true
                    isMarkerButtonPressed = true
                }
            }
        ) {
            currentLocation.value?.let {
                Marker(
                    state = MarkerState(position = it),
                    title = "My Location",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                )
            }

            markersToDisplay.filter {
                it.eventName.contains(
                    searchQuery.value.text,
                    ignoreCase = true
                )
            }
                .forEach { marker ->
                    Marker(
                        state = MarkerState(
                            position = LatLng(
                                marker.location.latitude,
                                marker.location.longitude
                            )
                        ),
                        title = marker.eventName,
                        icon = if (selectedColor == null || selectedColor == Color.Red) {
                            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                        } else {
                            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
                        },
                        onClick = {
                            val markerJson = Gson().toJson(marker)
                            navController.currentBackStackEntry?.savedStateHandle?.set(
                                "markerData",
                                markerJson
                            )
                            navController.navigate("landmark_details/${marker.id}")
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentSize(Alignment.TopStart),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = searchQuery.value,
                    onValueChange = { newValue ->
                        searchQuery.value = newValue
                        if (newValue.text.isBlank()) {
                            resetState()
                        }
                    },
                    placeholder = { Text("Search", color = Color.Red) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboardController?.hide()
                        }
                    )
                )
                IconButton(onClick = { expanded = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options",
                        tint = Color.Red
                    )
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
                            currentUserId?.let { navController.navigate("user_profile/${it}") }
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
                    DropdownMenuItem(
                        text = { Text("All Landmarks", color = Color.Black) },
                        onClick = {
                            expanded = false
                            navController.navigate("allLandmarks") // Nova ruta za navigaciju
                        }
                    )

                }
            }
            if (searchQuery.value.text.isNotBlank()) {
                matchingLandmarks.value.forEach { (landmarkName, imageUrl) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                val landmark = markers.find { it.eventName == landmarkName }
                                landmark?.let {
                                    val markerJson = Gson().toJson(it)
                                    navController.currentBackStackEntry?.savedStateHandle?.set(
                                        "markerData",
                                        markerJson
                                    )
                                    navController.navigate("landmark_details/${it.id}")
                                }
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = rememberImagePainter(imageUrl),
                            contentDescription = null,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.Gray)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = landmarkName,
                            color = Color.Black,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = {
                            navigateToLandmark(landmarkName)
                        }) {
                            Icon(
                                imageVector = Icons.Filled.MyLocation,
                                contentDescription = "Navigate to Landmark",
                                tint = Color.Red
                            )
                        }
                    }
                }
            }


            Spacer(modifier = Modifier.height(16.dp))
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
                            navController.currentBackStackEntry?.savedStateHandle?.set(
                                "location",
                                location
                            )
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
                Spacer(modifier = Modifier.width(16.dp))
                TextButton(
                    onClick = {
                        homeViewModel.resetFilter() // Resetuje filter
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red,
                        contentColor = Color.Black
                    )
                ) {
                    Text(
                        text = "Reset Filter",

                        )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    onClick = { recenterMap() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red,
                        contentColor = Color.Black
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "Recenter Map",
                        tint = Color.Black,
                    )
                }
            }
        }
    }
}
@Composable
fun LandmarkFilterDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Apply Filters") },
        text = { /* Add filter options here */ },
        confirmButton = {
            TextButton(
                onClick = { onDismiss() /* Apply filters */ }
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onDismiss() }
            ) {
                Text("Cancel")
            }
        }
    )
}

class HomeViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MarkerViewModel::class.java)) {
            return MarkerViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
