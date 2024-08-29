package pages

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.app1.view.LandmarkViewModel
import com.example.app1.view.LandmarkViewModelFactory
import com.example.app1.view.Marker
import com.google.gson.Gson

import coil.request.ImageRequest
import com.example.app1.view.AuthViewModel
import com.example.app1.data.Resource
import com.example.app1.view.UsersViewModel
import com.example.app1.data.Rate
import java.math.RoundingMode

@Composable
fun LandmarkDetailsPage(
    navController: NavController,
    landmarkId: String
) {
    val landmarkViewModel: LandmarkViewModel = viewModel(factory = LandmarkViewModelFactory())
    val landmarkResource by landmarkViewModel.landmarkDetail.collectAsState()
    val ratesResource by landmarkViewModel.rates.collectAsState()
    val viewModel: AuthViewModel = viewModel()

    val markerDataJson = navController.previousBackStackEntry
        ?.savedStateHandle
        ?.get<String>("markerData")

    val markerData = Gson().fromJson(markerDataJson, Marker::class.java)

    val usersViewModel: UsersViewModel = viewModel()
    var userName by remember { mutableStateOf("") }

    markerData?.userId?.let { userId ->
        usersViewModel.users.collectAsState().value.find { user -> user.id == userId }
            ?.let { user ->
                userName = user.fullName
            }
    }

    val rates = remember { mutableStateListOf<Rate>() }
    val averageRate = remember { mutableStateOf(0.0) }
    val isLoading = remember { mutableStateOf(false) }
    val showRateDialog = remember { mutableStateOf(false) }
    val myPrice = remember { mutableStateOf(0) }

    LaunchedEffect(landmarkId) {
        landmarkViewModel.getEventDetail(landmarkId)
    }

    val landmark = (landmarkResource as? Resource.Success)?.result

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

        landmark?.let {
            Text(
                text = it.eventName,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Red,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "User: $userName",
                color = Color.White,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(10.dp))
            CustomLandmarkRate(average = averageRate.value)

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

            if (it.mainImage?.isNotEmpty() == true) {
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

        Spacer(modifier = Modifier.height(16.dp))
        CustomRateButton(
            enabled = landmark?.userId != viewModel.getCurrentUser()?.uid,
            onClick = {
                val rateExist = rates.firstOrNull {
                    it.landmarkId == landmark?.id && it.userId == viewModel.getCurrentUser()?.uid
                }
                if (rateExist != null) {
                    myPrice.value = rateExist.rate
                }
                showRateDialog.value = true
            }
        )

        if (showRateDialog.value) {
            RateLandmarkDialog(
                showRateDialog = showRateDialog,
                rate = myPrice,
                rateBeach = {
                    val rateExist = rates.firstOrNull {
                        it.landmarkId == landmark?.id && it.userId == viewModel.getCurrentUser()?.uid
                    }
                    if (rateExist != null) {
                        isLoading.value = true
                        landmarkViewModel.updateRate(
                            rid = rateExist.id,
                            rate = myPrice.value
                        )
                    } else {
                        isLoading.value = true
                        landmarkViewModel.addRate(
                            bid = landmark?.id ?: "",
                            rate = myPrice.value,
                            landmark = landmark!!
                        )
                    }
                },
                isLoading = isLoading
            )
        }
    }

    ratesResource.let { resource ->
        when (resource) {
            is Resource.Success -> {
                Log.d("DataFetch", "Rates fetched successfully: ${resource.result}")
                rates.clear()  // Clear existing rates
                rates.addAll(resource.result)
                val sum = rates.sumOf { it.rate.toDouble() }
                averageRate.value = if (rates.isNotEmpty()) {
                    val rawAverage = sum / rates.size
                    rawAverage.toBigDecimal().setScale(1, RoundingMode.UP).toDouble()
                } else {
                    0.0
                }
            }

            is Resource.loading -> {
                // Handle loading state if needed
            }

            is Resource.Failure -> {
                Log.e("DataError", "Failed to fetch rates: ${resource.exception}")
            }
        }
    }

    landmarkViewModel.newRate.collectAsState().value.let { resource ->
        when (resource) {
            is Resource.Success -> {
                isLoading.value = false
                val existingRate = rates.firstOrNull { it.id == resource.result }
                if (existingRate != null) {
                    existingRate.rate = myPrice.value
                } else {
                    rates.add(
                        Rate(
                            id = resource.result,
                            rate = myPrice.value,
                            landmarkId = landmark?.id ?: "",
                            userId = viewModel.getCurrentUser()?.uid ?: ""
                        )
                    )
                }
                // Recalculate the average rate
                val sum = rates.sumOf { it.rate.toDouble() }
                averageRate.value = if (rates.isNotEmpty()) {
                    sum / rates.size
                } else {
                    0.0
                }
                Log.d("Rates", "Rates: $rates")
                Log.d("AverageCalculation", "Sum: $sum, Size: ${rates.size}, Average: ${averageRate.value}")

            }

            is Resource.loading -> {
                // Handle loading state
            }

            is Resource.Failure -> {
                val context = LocalContext.current
                Toast.makeText(context, "Error rating the landmark", Toast.LENGTH_LONG).show()
                isLoading.value = false
            }

            null -> {
                isLoading.value = false
            }
        }
    }
}



    @Composable
fun CustomRateButton(
    onClick: () -> Unit,
    enabled: Boolean
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(Color(0xFF6200EA), RoundedCornerShape(30.dp)),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF6200EA),
            contentColor = Color.Black,
            disabledContainerColor = Color(0xFFD3D3D3),
            disabledContentColor = Color.White
        ),
    ) {
        Text(
            "Rate landmark",
            style = TextStyle(
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        )
    }
}

@Composable
fun CustomLandmarkRate(
    average: Number
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = "",
            tint = Color.Yellow
        )
        Spacer(modifier = Modifier.width(5.dp))
        inputTextIndicator(textValue = "$average / 5")
    }
}

@Composable
fun inputTextIndicator(textValue: String) {
    Text(
        style = TextStyle(
            color = Color.Red,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        ),
        text = textValue
    )
}
