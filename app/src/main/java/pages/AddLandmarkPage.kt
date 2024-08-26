package pages

import android.widget.Toast
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.app1.LandmarkViewModel
import com.example.app1.Resource
import com.google.android.gms.maps.model.LatLng

@Composable
fun AddLandmarkPage(navController: NavController) {
    val landmarkViewModel: LandmarkViewModel = viewModel()
    val landmarkTypes = listOf("Crkva", "Spomenik", "Park", "Arheolosko nalaziste")
    var selectedEventType by remember { mutableStateOf("") }
    val EventName = remember { mutableStateOf(TextFieldValue("")) }
    val additionalDetails = remember { mutableStateOf(TextFieldValue("")) }
    val crowdLevel = remember { mutableStateOf(1) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val buttonIsLoading = remember { mutableStateOf(false) }
    val showedAlert = remember { mutableStateOf(false) }
    val eventFlow = landmarkViewModel?.landmarkflow?.collectAsState(initial = null)?.value
    var galleryImages by remember { mutableStateOf<List<Uri>>(emptyList()) }

    // Photo picker launcher
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    val location = remember { mutableStateOf<LatLng?>(null) }
    val savedStateHandle = navController.previousBackStackEntry?.savedStateHandle

    // Preuzimanje vrednosti iz SavedStateHandle-a
    LaunchedEffect(Unit) {
        val receivedLocation = savedStateHandle?.get<LatLng>("location")
        location.value = receivedLocation
    }

    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = { navController.navigateUp() }, // Navigira unazad
            modifier = Modifier
                .padding(bottom = 16.dp)
                .align(Alignment.Start),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
        ) {
            Text(text = "Back", color = Color.White)
        }

        Text(
            text = "Landmark Details",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Red,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Event Type Selection
        Text(text = "Select Landmark Type", fontWeight = FontWeight.Bold, color = Color.Red)
        landmarkTypes.forEach { eventType ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                RadioButton(
                    selected = (selectedEventType == eventType),
                    onClick = { selectedEventType = eventType },
                    colors = RadioButtonDefaults.colors(selectedColor = Color.Red)
                )
                Text(text = eventType, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Event Name Input
        TextField(
            value = EventName.value,
            onValueChange = { EventName.value = it },
            label = { Text("Landmark Name", color = Color.Red) },
            colors = TextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedIndicatorColor = Color.Red,
                unfocusedIndicatorColor = Color.Red
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Additional Details and Crowd Level for all events
        TextField(
            value = additionalDetails.value,
            onValueChange = { additionalDetails.value = it },
            label = {
                if (selectedEventType == "Crkva") {
                    Text("How did it occur?", color = Color.Red)
                } else {
                    Text("Describe the situation, crowd...", color = Color.Red)
                }
            },
            colors = TextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedIndicatorColor = Color.Red,
                unfocusedIndicatorColor = Color.Red
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Crowd Level (1-5)", fontWeight = FontWeight.Bold, color = Color.Red)
        Row {
            (1..5).forEach { level ->
                RadioButton(
                    selected = (crowdLevel.value == level),
                    onClick = { crowdLevel.value = level },
                    colors = RadioButtonDefaults.colors(selectedColor = Color.Red)
                )
                Text(text = level.toString(), color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Add Photo
        Box(
            modifier = Modifier
                .size(150.dp)
                .clickable {
                    launcher.launch("image/*")
                }
                .background(Color.DarkGray), // Use background modifier instead of backgroundColor
            contentAlignment = Alignment.Center
        ) {
            if (imageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(imageUri),
                    contentDescription = "Selected Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(text = "Add Photo", color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Submit Button
        Button(
            onClick = {
                showedAlert.value = false
                buttonIsLoading.value = true
                landmarkViewModel?.saveLandmarkData(
                    eventType = selectedEventType,
                    eventName = EventName.value.text,
                    description = additionalDetails.value.text,
                    crowd = crowdLevel.value,
                    mainImage = imageUri!!,
                    galleryImages = emptyList(),
                    location = location.value
                )
                // Pass data back to HomePage
                navController.previousBackStackEntry?.savedStateHandle?.set(
                    "newMarker",
                    Pair(location.value!!, EventName.value.text)
                )
                navController.navigateUp() // Navigate back
            },

            modifier = Modifier.align(Alignment.End),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
        ) {
            if (buttonIsLoading.value) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp))
            } else {
                Text(text = "Submit", color = Color.White)
            }
        }

        eventFlow?.let {
            when (it) {
                is Resource.Failure -> {
                    Log.d("LandmarkFlow", it.toString())
                    buttonIsLoading.value = false
                    val context = LocalContext.current
                    if (!showedAlert.value) {
                        showedAlert.value = true
                        val errorMessage = it.exception?.message
                            ?: "An unknown error occurred" // Replace with actual property
                        Toast.makeText(context, "Error: $errorMessage", Toast.LENGTH_SHORT).show()
                        landmarkViewModel?.getAllLandmarks()
                    }
                }

                is Resource.loading -> {
                    // Handle loading state if necessary
                }

                is Resource.Success -> {
                    Log.d("LandmarkFlow", it.toString())
                    buttonIsLoading.value = false
                    val context = LocalContext.current
                    if (!showedAlert.value) {
                        showedAlert.value = true
                        Toast.makeText(context, "Landmark added successfully!", Toast.LENGTH_SHORT)
                            .show()
                        landmarkViewModel?.getAllLandmarks()
                    }
                }

                null -> {}
            }
        }

    }
}
