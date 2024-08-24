package com.example.app1

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class Marker(
    val userId: String = "",
    val eventName: String = "",
    val eventType: String = "",
    val description: String = "",
    val crowd: Int = 0,
    val mainImage: String = "",
    val galleryImages: List<String> = emptyList(),
    val location: GeoPoint = GeoPoint(0.0, 0.0)
)

class MarkerViewModel(private val context: Context) : ViewModel() {
    private val _landmarks = MutableStateFlow<List<Marker>>(emptyList())
    val landmarks: StateFlow<List<Marker>> = _landmarks

    private val firestore = FirebaseFirestore.getInstance()
    private var markerListenerRegistration: ListenerRegistration? = null

    init {
        loadLandmarksFromFirebase()
    }

    fun addLandmark(latitude: Double, longitude: Double, name: String) {
        val newLandmark = Marker()
        firestore.collection("markers")
            .add(newLandmark)
            .addOnSuccessListener {
                Log.d("HomeViewModel", "Landmark added successfully")
            }
            .addOnFailureListener { e ->
                Log.e("HomeViewModel", "Error adding landmark: ", e)
            }
    }

    private fun loadLandmarksFromFirebase() {
        viewModelScope.launch {
            firestore.collection("markers").get().addOnSuccessListener { result ->
                val landmarksl = result.map { document ->
                    Marker(
                        userId = document.getString("userId") ?: "",
                        eventName = document.getString("eventName") ?: "",
                        eventType = document.getString("eventType") ?: "",
                        description = document.getString("description") ?: "",
                        crowd = document.getLong("crowd")?.toInt() ?: 0,
                        mainImage = document.getString("mainImage") ?: "",
                        galleryImages = document.get("galleryImages") as? List<String> ?: emptyList(),
                        location = document.getGeoPoint("location") ?: GeoPoint(0.0, 0.0)
                    )
                }
                _landmarks.value = landmarksl
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        markerListenerRegistration?.remove()
    }
}
