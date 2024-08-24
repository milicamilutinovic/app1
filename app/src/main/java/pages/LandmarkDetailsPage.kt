package pages

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.app1.LandmarkViewModel
import com.example.app1.LandmarkViewModelFactory
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

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
    val markerData = Gson().fromJson(markerDataJson, MarkerData::class.java)

    // Handle the state of the landmark details
    val landmark by landmarkViewModel.landmark.collectAsState()

    LaunchedEffect(markerData) {
        markerData?.let {
            // Fetch details for the specific landmark if needed
            // For example, landmarkViewModel.getLandmarkDetails(it.id)
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

        // Display the details of the landmark
        markerData?.let {
            Text(
                text = it.name,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Red,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "Latitude: ${it.latitude}",
                color = Color.White,
                fontSize = 16.sp
            )
            Text(
                text = "Longitude: ${it.longitude}",
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
