package com.example.app1.data

import android.net.Uri
import com.example.app1.service.DatabaseService
import com.example.app1.service.StorageService
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class LandmarkRepositoryImpl : LandmarkRepository {
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestoreInstance = FirebaseFirestore.getInstance()
    private val storageInstance = FirebaseStorage.getInstance()

    private val databaseService = DatabaseService(firestoreInstance)
    private val storageService = StorageService(storageInstance)


    override suspend fun getAllLandmarks(): Resource<List<Landmark>> {
        return try{
            val snapshot = firestoreInstance.collection("landmarks").get().await()
            val landmarks = snapshot.toObjects(Landmark::class.java)
            Resource.Success(landmarks)

        }catch (e: Exception){
            e.printStackTrace()
            Resource.Failure(e)
        }
    }

    override suspend fun saveLandmarkData(
        description: String,
        crowd: Int,
        eventName: String,
        eventType: String,
        mainImage: Uri,
        galleryImages: List<Uri>,
        location: LatLng
    ): Resource<String> {
        return try{
            val currentUser = firebaseAuth.currentUser
            if(currentUser!=null){
                val mainImageUrl = storageService.uploadLandmarkMainImage(mainImage)
                val galleryImagesUrls = storageService.uploadLandmarkGalleryImages(galleryImages)
                val geoLocation = GeoPoint(
                    location.latitude,
                    location.longitude
                )
                val landmark = Landmark(
                    userId = currentUser.uid,
                    description = description,
                    crowd = crowd,
                    eventName = eventName,
                    eventType = eventType,
                    mainImage = mainImageUrl,
                    galleryImages = galleryImagesUrls,
                    location = geoLocation
                )
                databaseService.saveLandmarkData(landmark)
                //databaseService.addPoints(currentUser.uid, 5)
            }
            Resource.Success("Uspesno sačuvani svi podaci o istrojiskoj zamenitsti")
        }catch (e: Exception){
            e.printStackTrace()
            Resource.Failure(e)
        }
    }

    override suspend fun getUserLandmark(uid: String): Resource<List<Landmark>> {
        return try {
            val snapshot = firestoreInstance.collection("landmarks")
                .whereEqualTo("userId", uid)
                .get()
                .await()
            val landmark = snapshot.toObjects(Landmark::class.java)
            Resource.Success(landmark)
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Failure(e)
        }
    }
    override suspend fun getLandmarkById(id: String): Resource<Landmark> {
        return try {
            val snapshot = firestoreInstance.collection("landmarks").document(id).get().await()
            val landmark = snapshot.toObject(Landmark::class.java)
            if (landmark != null) {
                Resource.Success(landmark)
            } else {
                Resource.Failure(Exception("Landmark not found"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Failure(e)
        }
    }

}