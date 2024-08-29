package com.example.app1


import RateRepositoryImpl
import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.aquaspot.model.Rate
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Firebase
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class LandmarkViewModel: ViewModel() {
    val repository = LandmarkRepositoryImpl()
    val rateRepository = RateRepositoryImpl()

    private val _landmarkFlow = MutableStateFlow<Resource<String>?>(null)
    val landmarkflow: StateFlow<Resource<String>?> = _landmarkFlow

    private val _newRate = MutableStateFlow<Resource<String>?>(null)
    val newRate: StateFlow<Resource<String>?> = _newRate

    private val _landmarks =
        MutableStateFlow<Resource<List<Landmark>>>(Resource.Success(emptyList()))
    val landmark: StateFlow<Resource<List<Landmark>>> get() = _landmarks

    private val _rates = MutableStateFlow<Resource<List<Rate>>>(Resource.Success(emptyList()))
    val rates: StateFlow<Resource<List<Rate>>> get() = _rates

    private val _landmarkDetail = MutableStateFlow<Resource<Landmark>?>(null)
    val landmarkDetail: StateFlow<Resource<Landmark>?> = _landmarkDetail

    // Existing methods...

    fun getLandmarkDetail(landmarkId: String) = viewModelScope.launch {
        _landmarkDetail.value = Resource.loading
        _landmarkDetail.value = repository.getLandmarkById(landmarkId)
        getLandmarkAllRates(landmarkId)
    }

    private val _userLandmarks =
        MutableStateFlow<Resource<List<Landmark>>>(Resource.Success(emptyList()))
    val userBeaches: StateFlow<Resource<List<Landmark>>> get() = _userLandmarks
    private val _filteredLandmarks = MutableLiveData<Resource<List<Landmark>>>()

    private val _averageRate = MutableStateFlow<Resource<Double>?>(null)
    val averageRate: StateFlow<Resource<Double>?> = _averageRate

    fun recalculateAverageRate(landmarkId: String) = viewModelScope.launch {
        _averageRate.value = rateRepository.recalculateAverageRate(landmarkId)
    }

    init {
        getAllLandmarks()
    }

    fun getAllLandmarks() = viewModelScope.launch {
        _landmarks.value = repository.getAllLandmarks()
        when (val result = _landmarks.value) {
            is Resource.Success -> {
                val landmarks = result.result
                Log.d("LandmarkViewModel", "Landmarks loaded successfully: $landmarks")
            }
            is Resource.Failure -> {
                val exception = result.exception
                Log.e("LandmarkViewModel", "Failed to load landmarks", exception)
            }
            is Resource.loading -> {
                Log.d("LandmarkViewModel", "Landmarks are loading")
            }
        }
    }
//ovo se menja

    fun saveLandmarkData(
        userId: String,
        eventType: String,
        eventName: String,
        description: String,
        crowd: Int,
        mainImage: Uri,
        galleryImages: List<Uri>,
        location: LatLng?
    ) {
        val storageRef = Firebase.storage.reference.child("images/${UUID.randomUUID()}")
        val uploadTask = storageRef.putFile(mainImage)

        uploadTask.addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                val landmarkData = hashMapOf(
                    "userId" to userId,
                    "eventType" to eventType,
                    "eventName" to eventName,
                    "description" to description,
                    "crowd" to crowd,
                    "mainImage" to uri.toString(),
                    "galleryImages" to galleryImages.map { it.toString() },
                    "location" to location?.let { GeoPoint(it.latitude, it.longitude) }
                )

                // Spremi landmarkData u bazu podataka
                Firebase.firestore.collection("landmarks")
                    .add(landmarkData)
                    .addOnSuccessListener {
                        // Uspješno spremljeno
                        _landmarkFlow.value = Resource.Success("Landmark successfully added!")
                    }
                    .addOnFailureListener {
                        // Greška prilikom spremanja
                        _landmarkFlow.value = Resource.Failure(it)
                    }
            }
        }.addOnFailureListener {
            // Greška prilikom upload-a slike
            _landmarkFlow.value = Resource.Failure(it)
        }
    }

    fun getEventDetail(eventId: String) = viewModelScope.launch {
        _landmarkDetail.value = Resource.loading
        _landmarkDetail.value = repository.getLandmarkById(eventId)

        getLandmarkAllRates(eventId)

    }

    fun getLandmarkAllRates(
        bid: String
    ) = viewModelScope.launch {
        _rates.value = Resource.loading
        val result = rateRepository.getLandmarksRates(bid)
        _rates.value = result
    }

    fun addRate(
        bid: String,
        rate: Int,
        landmark: Landmark
    ) = viewModelScope.launch {
        _newRate.value = rateRepository.addRate(bid, rate, landmark)
    }

    fun updateRate(
        rid: String,
        rate: Int
    ) = viewModelScope.launch {
        _newRate.value = rateRepository.updateRate(rid, rate)
      //  recalculateAverageRate(rid)
    }



    fun getUserLandmark(
        uid: String
    ) = viewModelScope.launch {
        _userLandmarks.value = repository.getUserLandmark(uid)
    }

    fun filterLandmarksByUserId(userId: String, onResult: (List<Landmark>) -> Unit) =
        viewModelScope.launch {
            // Dohvata sve događaje
            val allEvents = when (val result = repository.getAllLandmarks()) {
                is Resource.Success -> result.result ?: emptyList()
                else -> emptyList()
            }

            // Filtrira događaje koji odgovaraju `userId`
            val filteredList = allEvents.filter { event: Landmark ->
                Log.d(
                    "LandmarkViewModel",
                    "event.userId: ${event.userId}, userId: $userId"
                ) // Logovanje

                event.userId == userId
            }

            // Ažurira stanje sa filtriranim događajima
            _filteredLandmarks.value = Resource.Success(filteredList)

            // Vraćamo filtrirane događaje
            onResult(filteredList)
        }

    private val _selectedLandmark = MutableStateFlow<Landmark?>(null)
    val selectedLandmark: StateFlow<Landmark?> get() = _selectedLandmark

    // Funkcija za postavljanje izabranog događaja
    fun setSelectedLandmark(landmark: Landmark) {
        _selectedLandmark.value = landmark
    }



}
class LandmarkViewModelFactory:ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(LandmarkViewModel::class.java)){
            return LandmarkViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
