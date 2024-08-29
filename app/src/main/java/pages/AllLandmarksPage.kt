package pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.example.app1.data.Landmark
import com.example.app1.view.LandmarkViewModel
import com.example.app1.data.Resource
import com.google.android.gms.maps.model.LatLng

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllLandmarksPage(navController: NavController) {
    val landmarkViewModel: LandmarkViewModel = viewModel()
    val landmark by landmarkViewModel.landmark.collectAsState()

    LaunchedEffect(Unit) {
        landmarkViewModel.getAllLandmarks()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color.Black)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "All Landmarks",
                    style = MaterialTheme.typography.headlineSmall.copy(color = Color.Red)
                )
            },
            navigationIcon = {
                IconButton(onClick = { navController.navigate("home") }) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.Red
                    )
                }
            },
            colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color.Black)
        )

        when (landmark) {
            is Resource.Success -> {
                val eventList = (landmark as Resource.Success<List<Landmark>>).result
                val filteredEvents = eventList.filter { it.eventName.isNotEmpty() }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(filteredEvents) { event ->
                        LandmarkRow(
                            landmark = event,
                            navController = navController,
                            landmarkViewModel = landmarkViewModel
                        )
                    }
                }
            }
            is Resource.Failure -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Failed to load landmarks.",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            else -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.Red)
                }
            }
        }
    }
}

@Composable
fun LandmarkRow(landmark: Landmark, navController: NavController, landmarkViewModel: LandmarkViewModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.DarkGray),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color.Gray)
                ) {
                    Image(
                        painter = rememberImagePainter(landmark.mainImage),
                        contentDescription = "Landmark Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            item {
                Text(
                    text = landmark.description ?: "No Description",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                    modifier = Modifier
                        .width(150.dp)
                        .padding(end = 8.dp),
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 3
                )
            }

            item {
                Text(
                    text = "Crowd: ${landmark.crowd}",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                    modifier = Modifier.width(100.dp)
                )
            }

            item {
                Button(
                    onClick = {
                        val selectedLandmarkLocation = LatLng(landmark.location.latitude, landmark.location.longitude)
                        navController.currentBackStackEntry?.savedStateHandle?.set("selectedLandmarkLocation", selectedLandmarkLocation)
                        navController.navigate("home")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(text = "Show on Map", color = Color.White)
                }
            }

            item {
                Button(
                    onClick = {
                        navController.navigate("landmark_details/${landmark.id}")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(text = "See Landmark", color = Color.White)
                }
            }
        }
    }
}
