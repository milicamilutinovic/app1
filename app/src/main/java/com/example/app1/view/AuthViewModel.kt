package com.example.app1.view

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState
    fun getCurrentUser() = auth.currentUser
    init {
        checkAuthStatus()
    }

    fun checkAuthStatus() {
        if (auth.currentUser == null) {
            _authState.value = AuthState.Unauthenticated
        } else {
            _authState.value = AuthState.Authenticated
        }
    }

    fun login(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error("Email or password can't be empty")
            return
        }
        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.value = AuthState.Authenticated
                } else {
                    _authState.value =
                        AuthState.Error(task.exception?.message ?: "Something went wrong")
                }
            }
    }

    fun signup(email: String, password: String, fullName: String, phoneNumber: String, photoUri: Uri?) {
        if (email.isEmpty() || password.isEmpty() || fullName.isEmpty() || phoneNumber.isEmpty()) {
            _authState.value = AuthState.Error("All fields must be filled")
            return
        }
        _authState.value = AuthState.Loading

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: return@addOnCompleteListener

                    if (photoUri != null) {
                        Log.d("AuthViewModel", "Photo URI: $photoUri")
                        val storageRef = storage.reference.child("user_photos/$userId.jpg")
                        storageRef.putFile(photoUri)
                            .addOnSuccessListener {
                                Log.d("AuthViewModel", "Photo uploaded successfully")
                                storageRef.downloadUrl.addOnSuccessListener { uri ->
                                    Log.d("AuthViewModel", "Photo URL: $uri")
                                    saveUserDetails(userId, fullName, phoneNumber, uri.toString())
                                }.addOnFailureListener { exception ->
                                    Log.e("AuthViewModel", "Failed to get download URL", exception)
                                    _authState.value = AuthState.Error("Failed to get photo URL")
                                }
                            }
                            .addOnFailureListener { exception ->
                                Log.e("AuthViewModel", "Failed to upload photo", exception)
                                _authState.value = AuthState.Error("Failed to upload photo")
                            }
                    } else {
                        saveUserDetails(userId, fullName, phoneNumber, null)
                    }

                } else {
                    Log.e("AuthViewModel", "Signup failed", task.exception)
                    _authState.value =
                        AuthState.Error(task.exception?.message ?: "Something went wrong")
                }
            }
    }

    private fun saveUserDetails(userId: String, fullName: String, phoneNumber: String, photoUrl: String?) {
        val user = hashMapOf(
            "fullName" to fullName,
            "phoneNumber" to phoneNumber,
            "photoUrl" to photoUrl
        )

        firestore.collection("users").document(userId).set(user)
            .addOnSuccessListener {
                Log.d("AuthViewModel", "User details saved successfully")
                _authState.value = AuthState.Authenticated
            }
            .addOnFailureListener { exception ->
                Log.e("AuthViewModel", "Failed to save user details", exception)
                _authState.value = AuthState.Error("Failed to save user details")
            }
    }

    fun signout() {
        auth.signOut()
        _authState.value = AuthState.Unauthenticated
    }
}

sealed class AuthState {
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    object Loading : AuthState()
    data class Error(val message: String) : AuthState()
}
