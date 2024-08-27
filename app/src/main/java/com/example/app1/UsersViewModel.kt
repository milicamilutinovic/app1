package com.example.app1

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class User(
    val id:String,
    val fullName: String = "",
    val phoneNumber: String = "",
    val photoUrl: String? = null,
    val points: Int
)

class UsersViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users
    private val _userEvents = MutableStateFlow<Map<String, List<Landmark>>>(emptyMap()) // Mapa za događaje po korisniku
    val userEvents: StateFlow<Map<String, List<Landmark>>> = _userEvents

    init {
        fetchUsers()
    }

    private fun fetchUsers() {
        viewModelScope.launch {
            firestore.collection("users").get().addOnSuccessListener { result ->
                val userList = result.map { document ->
                    User(
                        id=document.id,
                        fullName = document.getString("fullName") ?: "",
                        phoneNumber = document.getString("phoneNumber") ?: "",
                        photoUrl = document.getString("photoUrl"),
                        points= document.getLong("points")?.toInt()?:0
                    )
                }
                _users.value = userList
            }
        }
    }
}