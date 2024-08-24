package com.example.app1



import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await

data class CustomUser(
    val fullName: String = "",
    val phoneNumber: String = "",
    val photoUrl: String? = null
)
class DatabaseService(
    private val firestore: FirebaseFirestore
) {
    suspend fun saveUserData(
        uid: String,
        user: CustomUser
    ): Resource<String>{
        return try {
            firestore.collection("users").document(uid).set(user).await()
            Resource.Success("Uspešno dodati podaci o korisniku")
        }catch (e: Exception){
            e.printStackTrace()
            Resource.Failure(e)
        }
    }

//    suspend fun addPoints(
//        uid: String,
//        points: Int
//    ): Resource<String>{
//        return try {
//            val userDocRef = firestore.collection("users").document(uid)
//            val userSnapshot = userDocRef.get().await()
//
//            if(userSnapshot.exists()){
//                val user = userSnapshot.toObject(CustomUser::class.java)
//                if(user != null){
//                    val newPoints = user.points + points
//                    userDocRef.update("points", newPoints).await()
//                    Resource.Success("Uspešno dodati poeni korisniku")
//                } else {
//                    Resource.Failure(Exception("Korisnik ne postoji"))
//                }
//            } else {
//                Resource.Failure(Exception("Korisnikov dokument ne postoji"))
//            }
//            Resource.Success("Uspešno dodati podaci o korisniku")
//        }catch (e: Exception){
//            e.printStackTrace()
//            Resource.Failure(e)
//        }
//    }

    suspend fun getUserData(
        uid: String
    ):Resource<String>{
        return try {
            val userDocRef = firestore.collection("users").document(uid)
            val userSnapshot = userDocRef.get().await()

            if(userSnapshot.exists()){
                val user = userSnapshot.toObject(CustomUser::class.java)
                if(user != null){
                    Resource.Success(user)
                } else {
                    Resource.Failure(Exception("Korisnik ne postoji"))
                }
            } else {
                Resource.Failure(Exception("Korisnikov dokument ne postoji"))
            }
            Resource.Success("Uspešno dodati podaci o korisniku")
        }catch (e: Exception){
            e.printStackTrace()
            Resource.Failure(e)
        }
    }


    suspend fun saveLandmarkData(
        landmark: Landmark
    ): Resource<String>{
        return try{
            firestore.collection("landmarks").add(landmark).await()
            Resource.Success("Uspešno sačuvani podaci o zamenitosti")
        }catch(e: Exception){
            e.printStackTrace()
            Resource.Failure(e)
        }
    }

//    suspend fun saveRateData(
//        rate: Rate
//    ): Resource<String>{
//        return try{
//            val result = firestore.collection("rates").add(rate).await()
//            Resource.Success(result.id)
//        }catch(e: Exception){
//            e.printStackTrace()
//            Resource.Failure(e)
//        }
//    }

//    suspend fun updateRate(
//        rid: String,
//        rate: Int
//    ): Resource<String>{
//        return try{
//            val documentRef = firestore.collection("rates").document(rid)
//            documentRef.update("rate", rate).await()
//            Resource.Success(rid)
//        }catch(e: Exception){
//            e.printStackTrace()
//            Resource.Failure(e)
//        }
//    }

}