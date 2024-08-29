package com.example.app1.data
import android.util.Log
import com.example.app1.service.DatabaseService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class RateRepositoryImpl : RateRepository {
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestoreInstance = FirebaseFirestore.getInstance()
    private val databaseService = DatabaseService(firestoreInstance)

    override suspend fun getLandmarksRates(
        lid: String
    ): Resource<List<Rate>> {
        return try {
            val rateDocRef = firestoreInstance.collection("rates")
            val querySnapshot = rateDocRef.get().await()
            val ratesList = mutableListOf<Rate>()
            for (document in querySnapshot.documents) {
                val landmarkId = document.getString("landmarkId") ?: ""
                if (landmarkId == lid) {
                    ratesList.add(
                        Rate(
                            id = document.id,
                            userId = document.getString("userId") ?: "",
                            landmarkId = lid,
                            rate = document.getLong("rate")?.toInt() ?: 0
                        )
                    )
                }
            }
            Resource.Success(ratesList)
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Failure(e)
        }
    }


    override suspend fun getUserRates(): Resource<List<Rate>> {
        return try{
            val rateDocRef = firestoreInstance.collection("rates")
            val querySnapshot = rateDocRef.get().await()
            val ratesList = mutableListOf<Rate>()
            for(document in querySnapshot.documents){
                val userId = document.getString("userId") ?: ""
                if(userId == firebaseAuth.currentUser?.uid){
                    ratesList.add(
                        Rate(
                        id = document.id,
                        landmarkId = document.getString("landmarkId") ?: "",
                        userId = userId,
                        rate = document.getLong("rate")?.toInt() ?: 0
                    )
                    )
                }
            }
            Resource.Success(ratesList)
        }catch (e: Exception){
            e.printStackTrace()
            Resource.Failure(e)
        }
    }

    override suspend fun getUserAdForLandmark(): Resource<List<Rate>> {
        TODO("Not yet implemented")
    }

    override suspend fun addRate(
        lid: String,
        rate: Int,
        landmark: Landmark
    ): Resource<String> {
        return try{
            val myRate = Rate(
                userId = firebaseAuth.currentUser!!.uid,
                landmarkId = lid,
                rate = rate
            )
            databaseService.addPoints(landmark.userId, rate * 3)
            val result = databaseService.saveRateData(myRate)
            result
        }catch (e: Exception){
            e.printStackTrace()
            Resource.Failure(e)
        }
    }

    override suspend fun updateRate(rid: String, rate: Int): Resource<String> {
        return try{
            val result = databaseService.updateRate(rid, rate)
            result
        }catch (e: Exception){
            e.printStackTrace()
            Resource.Failure(e)
        }
    }
    override suspend fun recalculateAverageRate(lid: String): Resource<Double> {
        return try {
            val result = getLandmarksRates(lid)
            when (result) {
                is Resource.Success -> {
                    val rates = result.result
                    val average = if (rates.isNotEmpty()) {
                        rates.map { it.rate }.average()
                    } else {
                        0.0
                    }
                    Resource.Success(average)
                }
                is Resource.Failure -> {
                    Log.e("com.example.app1.data.RateRepositoryImpl", "Failed to recalculate average rate: ${result.exception}")
                    Resource.Failure(result.exception)
                }

                Resource.loading -> TODO()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Failure(e)
        }
    }
}