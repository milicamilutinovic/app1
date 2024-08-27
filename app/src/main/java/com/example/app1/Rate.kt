package com.example.aquaspot.model

import com.google.firebase.firestore.DocumentId

data class Rate (
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val landmarkId: String = "",
    var rate: Int = 0
)