package com.example.app1

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine

data class Marker(
    @DocumentId val id:String="",
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
    private val landmarkViewModel: LandmarkViewModel by lazy {
        ViewModelProvider(
            (context as ViewModelStoreOwner)
        ).get(LandmarkViewModel::class.java)}

        private val _markers = MutableStateFlow<List<Marker>>(emptyList())
    val markers: StateFlow<List<Marker>> = _markers

    private val firestore = FirebaseFirestore.getInstance()
    private var markerListenerRegistration: ListenerRegistration? = null
    //dodajem ove dve
    private val _filteredMarkers = MutableLiveData<List<Marker>>() // Lista filtriranih markera
    val filteredMarkers: LiveData<List<Marker>> get() = _filteredMarkers
    //dodato
    private val _isFilterApplied = MutableLiveData<Boolean>(false) // Stanje da li je filter primenjen
    val isFilterApplied: LiveData<Boolean> get() = _isFilterApplied
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
                        doc.toObject(Marker::class.java).copy(id = doc.id)
                    }
                    _markers.value = markerList
                }
            }
    }
    fun getUserIdByNameFromFirestore(fullName: String, onResult: (String?) -> Unit) {
        //   val firestore = FirebaseFirestore.getInstance()

        firestore.collection("users")
            .whereEqualTo("fullName", fullName)
            .get()
            .addOnSuccessListener { documents ->
                if (documents != null && !documents.isEmpty) {
                    // Pretpostavljamo da je ime i prezime jedinstveno, pa uzimamo prvi dokument
                    val userId = documents.documents[0].id
                    onResult(userId)
                } else {
                    // Korisnik nije pronađen
                    onResult(null)
                }
            }
            .addOnFailureListener { exception ->
                // U slučaju greške, vratiti null
                onResult(null)
            }
    }

    fun filterMarkers(category: String,eventName: String, crowdLevel: Int) {
        val filtered = _markers.value?.filter { marker ->

            // Proveravamo da li marker odgovara kategoriji
            val matchesCategory = (category != "Select Category" && marker.eventType == category)
            // Proveravamo da li marker odgovara imenu događaja
            val matchesEventName = (eventName != "Select Event Name" && marker.eventName == eventName)
            // Proveravamo da li marker odgovara nivou gužve
            val matchesCrowdLevel = (crowdLevel != 0 && marker.crowd == crowdLevel)
            Log.d("MarkerViewModel","crowd:${marker.crowd}")




            // Na osnovu uslova se koristi logički "I"
            matchesCategory || matchesEventName || matchesCrowdLevel  // Svi uslovi se povezuju sa logičkim "ILI"
        } ?: emptyList()

        // Postavljamo filtrirane markere
        _filteredMarkers.value = filtered
        _isFilterApplied.value = true // Obeležavamo da je filter primenjen

        Log.d("MarkerViewModel", "Filtered markers: ${filtered.joinToString { it.eventName }}")
    }

    fun filterMarkersByUserName(fullName: String, onResult: (List<Marker>) -> Unit) {
        getUserIdByNameFromFirestore(fullName) { userId ->
            Log.d("MarkerViewModel", "Dobijeni userId: $userId")

            if (userId != null) {
                // Ako je pronađen ID, filtriramo markere
                val filteredMarkers = _markers.value?.filter { marker ->
                    //ovo sam dodala
                    Log.d("MarkerViewModel", "Marker userId: $marker.userId")

                    marker.userId == userId
                } ?: emptyList()

                // Vraćamo filtrirane markere
                onResult(filteredMarkers)
                _filteredMarkers.value = filteredMarkers // Resetovanje filtriranih markera

                _isFilterApplied.value = true // Resetovanje filtera
            } else {
                // Ako korisnik nije pronađen, vraćamo praznu listu
                onResult(emptyList())
            }
        }
    }


    fun resetFilter() {
        _isFilterApplied.value = false // Resetovanje filtera
        _filteredMarkers.value = emptyList() // Resetovanje filtriranih markera
    }
    override fun onCleared() {
        super.onCleared()
        markerListenerRegistration?.remove()

    }
    private fun convertFilteredEventsToMarkers(events: List<Landmark>): List<Marker> {
        return events.map { landmark ->
            Marker(
                id = landmark.id,
                userId = landmark.userId,
                eventName = landmark.eventName,
                eventType = landmark.eventType,
                description = landmark.description!!,
                crowd = landmark.crowd!!,
                mainImage = landmark.mainImage!!,
                galleryImages = landmark.galleryImages,
                location = landmark.location
            )
        }
    }
}
