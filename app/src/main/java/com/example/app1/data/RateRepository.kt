package com.example.app1.data

interface RateRepository {
    suspend fun getLandmarksRates(
        bid: String
    ): Resource<List<Rate>>
    suspend fun getUserRates(): Resource<List<Rate>>
    suspend fun getUserAdForLandmark(): Resource<List<Rate>>
    suspend fun addRate(
        lid: String,
        rate: Int,
        landmark: Landmark
    ): Resource<String>

    suspend fun updateRate(
        rid: String,
        rate: Int,
    ): Resource<String>
    suspend fun recalculateAverageRate(lid: String): Resource<Double>
}