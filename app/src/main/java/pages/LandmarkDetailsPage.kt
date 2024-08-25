package pages

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.app1.LandmarkViewModel
import com.example.app1.LandmarkViewModelFactory
import com.example.app1.Marker
import com.google.gson.Gson

import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.app1.UsersViewModel

@Composable
fun LandmarkDetailsPage(
    navController: NavController,
    landmarkViewModel: LandmarkViewModel = viewModel(factory = LandmarkViewModelFactory())
) {
    // Retrieve JSON string from the previous screen
    val markerDataJson = navController.previousBackStackEntry
        ?.savedStateHandle
        ?.get<String>("markerData")

    // Parse JSON string to MarkerData object
    val markerData = Gson().fromJson(markerDataJson, Marker::class.java)

    // Handle the state of the landmark details
    val landmark by landmarkViewModel.landmark.collectAsState()
    val  usersViewModel: UsersViewModel = viewModel() // Inicijalizacija UsersViewModel

    var userName by remember { mutableStateOf("") }

            markerData?.userId?.let { userId ->
                usersViewModel.users.collectAsState().value.find { user -> user.id == userId }
                    ?.let { user ->
                        userName = "${user.fullName}"
                    }
            }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Button(
            onClick = { navController.navigateUp() },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
        ) {
            Text(text = "Back", color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display the details of the marker
        markerData?.let {
            Text(
                text = it.eventName,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Red,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "User: ${userName}",
                color = Color.White,
                fontSize = 16.sp
            )

            Text(
                text = "Event Name: ${it.eventName}",
                color = Color.White,
                fontSize = 16.sp
            )

            Text(
                text = "Event Type: ${it.eventType}",
                color = Color.White,
                fontSize = 16.sp
            )

            Text(
                text = "Description: ${it.description}",
                color = Color.White,
                fontSize = 16.sp
            )

            Text(
                text = "Crowd Level: ${it.crowd}",
                color = Color.White,
                fontSize = 16.sp
            )

            // Display main image if available
            if (it.mainImage.isNotEmpty()) {
                Log.d("ImageLoad", "Loading image from URL: ${it.mainImage}")

                Spacer(modifier = Modifier.height(16.dp))
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(it.mainImage)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Main Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(Color.Gray)
                )
            }

            // Display gallery images if available
            if (it.galleryImages.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Gallery Images:",
                    color = Color.White,
                    fontSize = 16.sp
                )
                it.galleryImages.forEach { imageUrl ->
                    Spacer(modifier = Modifier.height(8.dp))
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Gallery Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(Color.Gray)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Latitude: ${it.location.latitude}",
                color = Color.White,
                fontSize = 16.sp
            )
            Text(
                text = "Longitude: ${it.location.longitude}",
                color = Color.White,
                fontSize = 16.sp
            )
        } ?: run {
            Text(
                text = "No landmark data available",
                color = Color.Red,
                fontSize = 16.sp
            )
        }
    }
}