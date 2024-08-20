package pages

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.app1.LocationService

@Composable
fun LocationServicePage(modifier: Modifier = Modifier, navController: NavController) {
    val context = LocalContext.current
    val locationState = remember { mutableStateOf("Location not available") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black) // Black background
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top // Align content to the top
    ) {
        // Back button positioned in the top-left corner
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .align(Alignment.Start)
                .padding(top = 16.dp) // Adjust padding to position at the top
        ) {
            Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.Red)
        }

        Spacer(modifier = Modifier.height(64.dp)) // Space to push content down

        // Title
        Text(
            text = "Do you want to get updated every moment?",
            fontSize = 24.sp,
            color = Color.Red,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        // Display updated location
        Text(
            text = locationState.value,
            fontSize = 18.sp,
            color = Color.White, // White text for visibility
            modifier = Modifier.padding(vertical = 16.dp)
        )

        // "Yes" and "No" buttons
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = {
                    startLocationService(context)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(text = "Yes", fontSize = 18.sp, color = Color.White)
            }
            Button(
                onClick = {
                    stopLocationService(context)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(text = "No", fontSize = 18.sp, color = Color.White)
            }
        }
    }
}

private fun startLocationService(context: Context) {
    Log.d("LocationService", "Starting location service")
    val serviceIntent = Intent(context, LocationService::class.java)
    ContextCompat.startForegroundService(context, serviceIntent)

    // Show notification
    Toast.makeText(context, "Location service started", Toast.LENGTH_SHORT).show()
}

private fun stopLocationService(context: Context) {
    val serviceIntent = Intent(context, LocationService::class.java)
    context.stopService(serviceIntent)

    // Show notification
    Toast.makeText(context, "Location service stopped", Toast.LENGTH_SHORT).show()
}
