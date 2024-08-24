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
    private val _markers = MutableStateFlow<List<Marker>>(emptyList())
    val markers: StateFlow<List<Marker>> = _markers

    private val firestore = FirebaseFirestore.getInstance()
    private var markerListenerRegistration: ListenerRegistration? = null

    init {
        loadMarkers()
    }

    fun addMarker(latitude: Double, longitude: Double, name: String) {
        val newLandmark = Marker()
        firestore.collection("landmarks")
            .add(newLandmark)
            .addOnSuccessListener {
                Log.d("HomeViewModel", "Landmark added successfully")
            }
            .addOnFailureListener { e ->
                Log.e("HomeViewModel", "Error adding landmark: ", e)
            }
    }
    private var listenerRegistration: ListenerRegistration? = null


    //druga zimena
    private fun loadMarkers() {
        listenerRegistration = firestore.collection("landmarks")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("MarkerViewModel", "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    val markerList = snapshots.mapNotNull { doc ->
                        doc.toObject(Marker::class.java).copy(userId = doc.id)
                    }
                    _markers.value = markerList
                }
            }
    }

    //promenila ovo
//    private fun loadMarkersFromFirebase() {
//        firestore.collection("landmarks")
//            .addSnapshotListener { snapshot, e ->
//                if (e != null) {
//                    Log.w("MarkerViewModel", "Listen failed.", e)
//                    return@addSnapshotListener
//                }
//
//                if (snapshot != null && !snapshot.isEmpty) {
//                    val markerList = snapshot.documents.map { document ->
//                        Marker(
//                            userId = document.getString("userId") ?: "",
//                            eventName = document.getString("eventName") ?: "",
//                            eventType = document.getString("eventType") ?: "",
//                            description = document.getString("description") ?: "",
//                            crowd = document.getLong("crowd")?.toInt() ?: 0,
//                            mainImage = document.getString("mainImage") ?: "",
//                            galleryImages = document.get("galleryImages") as? List<String> ?: emptyList(),
//                            location = document.getGeoPoint("location") ?: GeoPoint(0.0, 0.0)
//                        )
//                    }
//                    _markers.value = markerList
//                } else {
//                    Log.d("MarkerViewModel", "Current data: null")
//                }
//            }
//    }

    override fun onCleared() {
        super.onCleared()
        markerListenerRegistration?.remove()
    }
}
