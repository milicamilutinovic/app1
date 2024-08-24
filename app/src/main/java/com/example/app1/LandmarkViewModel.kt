package com.example.app1


import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LandmarkViewModel: ViewModel() {
    val repository = LandmarkRepositoryImpl()
    val rateRepository = LandmarkRepositoryImpl()

    private val _landmarkFlow = MutableStateFlow<Resource<String>?>(null)
    val landmarkflow: StateFlow<Resource<String>?> = _landmarkFlow

    private val _newRate = MutableStateFlow<Resource<String>?>(null)
    val newRate: StateFlow<Resource<String>?> = _newRate

    private val _landmarks = MutableStateFlow<Resource<List<Landmark>>>(Resource.Success(emptyList()))
    val landmark: StateFlow<Resource<List<Landmark>>> get() = _landmarks

//    private val _rates = MutableStateFlow<Resource<List<Rate>>>(Resource.Success(emptyList()))
//    val rates: StateFlow<Resource<List<Rate>>> get() = _rates
private val _landmarkDetail = MutableStateFlow<Resource<Landmark>?>(null)
    val landmarkDetail: StateFlow<Resource<Landmark>?> = _landmarkDetail

    // Existing methods...

    fun getLandmarkDetail(landmarkId: String) = viewModelScope.launch {
        _landmarkDetail.value = Resource.loading
        _landmarkDetail.value = repository.getLandmarkById(landmarkId)
    }

    private val _userLandmarks = MutableStateFlow<Resource<List<Landmark>>>(Resource.Success(emptyList()))
    val userBeaches: StateFlow<Resource<List<Landmark>>> get() = _userLandmarks

    init {
        getAllLandmarks()
    }

    fun getAllLandmarks() = viewModelScope.launch {
        _landmarks.value = repository.getAllLandmark()
    }

    fun saveLandmarkData(
        description: String,
        crowd: Int,
        eventName: String,
        eventType: String,
        mainImage: Uri,
        galleryImages: List<Uri>,
        location: LatLng?
    ) = viewModelScope.launch{
        _landmarkFlow.value = Resource.loading
        repository.saveLandmarkData(
            description = description,
            crowd = crowd,
            eventName = eventName,
            eventType = eventType,
            mainImage = mainImage,
            galleryImages = galleryImages,
            location = location!!
        )
        _landmarkFlow.value = Resource.Success("Uspešno dodat objekat")
    }


//    fun getLandmarkAllRates(
//        bid: String
//    ) = viewModelScope.launch {
//        _rates.value = Resource.loading
//        val result = rateRepository.getLandmarkRates(bid)
//        _rates.value = result
//    }

//    fun addRate(
//        bid: String,
//        rate: Int,
//        landmark: Landmark
//    ) = viewModelScope.launch {
//        _newRate.value = rateRepository.addRate(bid, rate, landmark)
//    }

//    fun updateRate(
//        rid: String,
//        rate: Int
//    ) = viewModelScope.launch{
//        _newRate.value = rateRepository.updateRate(rid, rate)
//    }

    fun getUserLandmark(
        uid: String
    ) = viewModelScope.launch {
        _userLandmarks.value = repository.getUserLandmark(uid)
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