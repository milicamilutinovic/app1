package pages


import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.app1.Landmark
import com.example.app1.LandmarkViewModel
import com.example.app1.MarkerViewModel
import com.example.app1.Resource
import com.example.app1.User
import com.example.app1.UsersViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Composable
fun LandmarkFilterDialog(
    onDismiss: () -> Unit,
    landmarkViewModel: LandmarkViewModel = viewModel(),
    usersViewModel: UsersViewModel = viewModel()
) {
    var isCategoryDropdownExpanded by remember { mutableStateOf(false) }
    var isUserDropdownExpanded by remember { mutableStateOf(false) }
    var isEventNameDropdownExpanded by remember { mutableStateOf(false) }
    var ChooseUser by remember { mutableStateOf<User?>(null) }
    var ChooseEventName by remember { mutableStateOf("Select Landmark Name") }
    val usersState by usersViewModel.users.collectAsState()
    var isCrowdLevelDropdownExpanded by remember { mutableStateOf(false) }
    var selectedCrowdLevel by remember { mutableStateOf(1) }

    var Category by remember { mutableStateOf("Select Category") }

    val eventsResource by landmarkViewModel.landmark.collectAsState()
    val eventsState = when (eventsResource) {
        is Resource.Success -> (eventsResource as Resource.Success<List<Landmark>>).result
        is Resource.Failure -> emptyList()
        is Resource.loading -> emptyList()
    }

    val markerViewModel: MarkerViewModel = viewModel()

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Text(
                text = "Select Landmark Details",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                // Category Dropdown Menu
                TextButton(onClick = { isCategoryDropdownExpanded = !isCategoryDropdownExpanded }) {
                    Text(Category)
                }

                DropdownMenu(
                    expanded = isCategoryDropdownExpanded,
                    onDismissRequest = { isCategoryDropdownExpanded = false }
                ) {
                    listOf("Crkva", "Spomenik", "Park", "Arheolosko nalaziste").forEach { cat ->
                        DropdownMenuItem(
                            onClick = {
                                Category = cat
                                isCategoryDropdownExpanded = false
                            },
                            text = { Text(cat) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Event Name Dropdown Menu
                TextButton(onClick = { isEventNameDropdownExpanded = !isEventNameDropdownExpanded }) {
                    Text(ChooseEventName)
                }

                DropdownMenu(
                    expanded = isEventNameDropdownExpanded,
                    onDismissRequest = { isEventNameDropdownExpanded = false }
                ) {
                    eventsState.forEach { event ->
                        DropdownMenuItem(
                            onClick = {
                                ChooseEventName = event.eventName
                                isEventNameDropdownExpanded = false
                            },
                            text = { Text(event.eventName) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // User Dropdown Menu
                TextButton(onClick = { isUserDropdownExpanded = !isUserDropdownExpanded }) {
                    Text(ChooseUser?.let { "${it.fullName}" } ?: "Select User")
                }

                DropdownMenu(
                    expanded = isUserDropdownExpanded,
                    onDismissRequest = { isUserDropdownExpanded = false }
                ) {
                    usersState.forEach { user ->
                        DropdownMenuItem(
                            onClick = {
                                ChooseUser = user
                                isUserDropdownExpanded = false
                            },
                            text = { Text("${user.fullName}") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Crowd Level Dropdown Menu
                TextButton(onClick = { isCrowdLevelDropdownExpanded = !isCrowdLevelDropdownExpanded }) {
                    Text("Crowd Level: $selectedCrowdLevel")
                }

                DropdownMenu(
                    expanded = isCrowdLevelDropdownExpanded,
                    onDismissRequest = { isCrowdLevelDropdownExpanded = false }
                ) {
                    (1..5).forEach { level ->
                        DropdownMenuItem(
                            onClick = {
                                selectedCrowdLevel = level
                                isCrowdLevelDropdownExpanded = false
                            },
                            text = { Text("Level $level") }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if(ChooseUser!=null) {
                    markerViewModel.filterMarkersByUserName(
                        ChooseUser!!.fullName
                    ){filteredMarkers ->}
                }
                else{
                    markerViewModel.filterMarkers(
                        Category,
                        ChooseEventName,
                        selectedCrowdLevel)
                }
                onDismiss()
            }) {
                Text("Filter")
            }
        }
    )
}
