package com.example.app1


import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.GeoPoint
import java.io.Serializable

data class Landmark(
    @DocumentId val id: String = "",
    val userId: String = "",
    val eventName: String,
    val eventType: String,
    val description: String? = null, // Nullable, as it won't be used for Natural Disasters
    val crowd: Int? = null, // Nullable, as it won't be used for Natural Disasters
    val mainImage: String? = null, // Nullable, as the image might not be selected
    val galleryImages: List<String> = emptyList(),
    val location: GeoPoint = GeoPoint(0.0, 0.0)
    //dodala Seriazable za prikaz landmarka
):Serializable{
    constructor() : this(
        id = "",
        userId = "",
        eventName = "",
        eventType = "",
        description = null,
        crowd = null,
        mainImage = null,
        galleryImages = emptyList(),
        location = GeoPoint(0.0, 0.0)
    )
}