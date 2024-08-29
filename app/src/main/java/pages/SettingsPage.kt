package pages

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.app1.data.Landmark
import com.example.app1.location.LocationService

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun LocationServicePage(modifier: Modifier = Modifier, navController: NavController) {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    val locationState = remember { mutableStateOf("Location not available") }
    val isTrackingServiceEnabled = sharedPreferences.getBoolean("tracking_location", true)
    val checked = remember { mutableStateOf(isTrackingServiceEnabled) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black) // Black background
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Back button positioned in the top-left corner
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .align(Alignment.Start)
                .padding(top = 16.dp)
        ) {
            Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.Red)
        }

        Spacer(modifier = Modifier.height(16.dp)) // Space to push content down

        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Red, RoundedCornerShape(bottomEnd = 50.dp, bottomStart = 50.dp))
                .height(200.dp)
                .padding(top = 60.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Location Service Settings",
                style = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                ),
            )
        }

        Spacer(modifier = Modifier.height(24.dp)) // Space between header and content

        // Title
        Text(
            text = "Do you want to get updated every moment?",
            fontSize = 20.sp,
            color = Color.Red,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        // Display updated location
        Text(
            text = locationState.value,
            fontSize = 18.sp,
            color = Color.White,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        // Service toggle switch
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color.Gray)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(5.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Track Location",
                    style = TextStyle(
                        fontSize = 16.sp
                    )
                )
                Switch(
                    checked = checked.value,
                    onCheckedChange = {
                        checked.value = it
                        if (it) {
                            Intent(context, LocationService::class.java).apply {
                                action = LocationService.ACTION_FIND_NEARBY
                                context.startForegroundService(this)
                            }
                            with(sharedPreferences.edit()) {
                                putBoolean("tracking_location", true)
                                apply()
                            }
                        } else {
                            Intent(context, LocationService::class.java).apply {
                                action = LocationService.ACTION_STOP
                                context.stopService(this)
                            }
                            Intent(context, LocationService::class.java).apply {
                                action = LocationService.ACTION_START
                                context.startForegroundService(this)
                            }
                            with(sharedPreferences.edit()) {
                                putBoolean("tracking_location", false)
                                apply()
                            }
                        }
                    },
                    thumbContent = if (checked.value) {
                        {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                            )
                        }
                    } else {
                        null
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Red,
                        checkedTrackColor = Color.LightGray,
                        uncheckedThumbColor = Color.Gray,
                        uncheckedTrackColor = Color.White
                    )
                )
            }
        }
    }
}
