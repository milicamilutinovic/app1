package pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.app1.AuthViewModel
import com.example.app1.Landmark
import com.example.app1.LandmarkRepositoryImpl
import com.example.app1.LandmarkViewModel
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun UserProfilePage(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val authState by authViewModel.authState.observeAsState()
    val user = authViewModel.getCurrentUser()
    val userId = user?.uid

    var fullName by remember { mutableStateOf<String?>(null) }
    var phoneNumber by remember { mutableStateOf<String?>(null) }
    var photoUrl by remember { mutableStateOf<String?>(null) }

    val landmarkViewModel: LandmarkViewModel = viewModel()
    var landmarks by remember { mutableStateOf<List<Landmark>>(emptyList()) } // List of user's events

    LaunchedEffect(userId) {
        userId?.let {
            val userDocument = FirebaseFirestore.getInstance().collection("users").document(it)
            userDocument.get().addOnSuccessListener { document ->
                if (document != null) {
                    fullName = document.getString("fullName")
                    phoneNumber = document.getString("phoneNumber")
                    photoUrl = document.getString("photoUrl")
                }
            }

            landmarkViewModel.filterLandmarksByUserId(it) { userLandmarks ->
                landmarks = userLandmarks
            }
        }
    }


    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "User Profile",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Red
        )

        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .size(150.dp)
                .background(Color.Gray, shape = CircleShape)
        ) {
            if (photoUrl != null) {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = "Profile Picture",
                    contentScale = ContentScale.Crop, // Ensures the image fills the circle
                    modifier = Modifier
                        .size(150.dp)
                        .clip(CircleShape) // Clips the image to a circular shape
                        .align(Alignment.Center)
                )
            } else {
                Text(
                    text = "No Image",
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Name: ${fullName ?: "Loading..."} ",
            fontSize = 24.sp,
            color = Color.Red
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Phone: ${phoneNumber ?: "Loading..."}",
            fontSize = 24.sp,
            color = Color.Red
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                authViewModel.signout()
                navController.navigate("login") {
                    popUpTo("home") { inclusive = true }
                }
            },
            modifier = Modifier
                .size(width = 180.dp, height = 48.dp)
                .align(Alignment.CenterHorizontally),
            shape = CircleShape,
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color.Red)
        ) {
            Text(text = "Log Out", fontSize = 18.sp, color = Color.White)
        }

        Spacer(modifier = Modifier.height(14.dp))
        // Display user's events
        Text(
            text = "User's Landmarks:",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2589a0)
        )

        Spacer(modifier = Modifier.height(16.dp))

        landmarks.forEach { event ->
            Column(
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Text(text = "Name: ${event.eventName}", fontSize = 20.sp, color = Color.Red)
                Text(text = "Description: ${event.description}", fontSize = 16.sp, color = Color.Red)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { navController.navigate("home") },
            shape = CircleShape,
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color.Red),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .width(180.dp)
        ) {
            Text(text = "Back", fontSize = 18.sp, color = Color.White)
        }
    }
}
