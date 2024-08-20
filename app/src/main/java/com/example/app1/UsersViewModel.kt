package com.example.app1

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class User(
    val fullName: String = "",
    val phoneNumber: String = "",
    val photoUrl: String? = null
)

class UsersViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users

    init {
        fetchUsers()
    }

    private fun fetchUsers() {
        viewModelScope.launch {
            firestore.collection("users").get().addOnSuccessListener { result ->
                val userList = result.map { document ->
                    User(
                        fullName = document.getString("fullName") ?: "",
                        phoneNumber = document.getString("phoneNumber") ?: "",
                        photoUrl = document.getString("photoUrl")
                    )
                }
                _users.value = userList
            }
        }
    }
}