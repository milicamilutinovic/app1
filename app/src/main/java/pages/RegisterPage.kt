package pages

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.example.app1.AuthState
import com.example.app1.AuthViewModel

@Composable
fun RegisterPage(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<Uri?>(null) }

    val authState = authViewModel.authState.observeAsState()
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        photoUri = uri
    }

    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Authenticated -> navController.navigate("home")
            is AuthState.Error -> Toast.makeText(
                context,
                (authState.value as AuthState.Error).message, Toast.LENGTH_SHORT
            ).show()
            else -> Unit
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()), // Enable scrolling
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Register",
            fontSize = 36.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.Red,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(text = "Email", fontWeight = FontWeight.Bold, color = Color.Red) },
            leadingIcon = { Icon(Icons.Filled.Email, contentDescription = "Email Icon", tint = Color.Red, modifier = Modifier.size(24.dp)) },
            textStyle = TextStyle(color = Color.Red),
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black)
                .padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(text = "Password", fontWeight = FontWeight.Bold, color = Color.Red) },
            leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = "Password Icon", tint = Color.Red, modifier = Modifier.size(24.dp)) },
            visualTransformation = PasswordVisualTransformation(),
            textStyle = TextStyle(color = Color.Red),
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black)
                .padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text(text = "Full Name", fontWeight = FontWeight.Bold, color = Color.Red) },
            leadingIcon = { Icon(Icons.Filled.Person, contentDescription = "Full Name Icon", tint = Color.Red, modifier = Modifier.size(24.dp)) },
            textStyle = TextStyle(color = Color.Red),
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black)
                .padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text(text = "Phone Number", fontWeight = FontWeight.Bold, color = Color.Red) },
            leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = "Phone Number Icon", tint = Color.Red, modifier = Modifier.size(24.dp)) },
            isError = !isValidPhoneNumber(phoneNumber),
            textStyle = TextStyle(color = Color.Red),
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black)
                .padding(bottom = 32.dp)
        )

        Box(
            modifier = Modifier
                .size(120.dp)
                .background(Color.Gray, shape = RoundedCornerShape(8.dp))
                .padding(8.dp),
            contentAlignment = Alignment.Center // Center the text inside the box
        ) {
            photoUri?.let {
                val painter = rememberImagePainter(it)
                Image(
                    painter = painter,
                    contentDescription = "Selected Photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } ?: run {
                Text(
                    text = "No photo selected",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { launcher.launch("image/*") },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.Camera, contentDescription = "Select Photo Icon", tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Select Photo", fontWeight = FontWeight.Bold, color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (email.isEmpty() || password.isEmpty() || fullName.isEmpty() || phoneNumber.isEmpty() || photoUri == null) {
                    Toast.makeText(context, "All fields must be filled", Toast.LENGTH_SHORT).show()
                } else {
                    authViewModel.signup(email, password, fullName, phoneNumber, photoUri)
                }
            },
            enabled = authState.value != AuthState.Loading,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Create Account", fontWeight = FontWeight.Bold, color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { navController.navigate("login") }) {
            Text(text = "Already have an account? Log in", fontWeight = FontWeight.Bold, color = Color.Red)
        }
    }
}

fun isValidPhoneNumber(phoneNumber: String): Boolean {
    return phoneNumber.length in 10..15 && phoneNumber.all { it.isDigit() }
}
