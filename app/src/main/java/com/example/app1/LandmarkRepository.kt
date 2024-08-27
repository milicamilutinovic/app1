package com.example.app1

import android.net.Uri
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.GeoPoint
import com.google.rpc.context.AttributeContext

interface LandmarkRepository {

    suspend fun getAllLandmarks(): Resource<List<Landmark>>
    suspend fun saveLandmarkData(
        description: String,
        crowd: Int,
        eventName: String,
        eventType: String,
        mainImage: Uri,
        galleryImages: List<Uri>,
        location: LatLng
    ): Resource<String>

    suspend fun getUserLandmark(
        uid: String
    ): Resource<List<Landmark>>

    suspend fun getLandmarkById(id: String): Resource<Landmark>

    //suspend fun  recalculateAverageRate(landmarkId: String)
}