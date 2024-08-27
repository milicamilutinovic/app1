
import android.util.Log
import com.example.app1.Landmark
import com.example.app1.Resource
import com.example.aquaspot.model.Rate

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