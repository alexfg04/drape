package com.drape.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

/**
 * Data model representing a clothing item in the user's wardrobe.
 */
data class ClothingItem(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val imageUrl: String = "",
    val name: String = "",
    val brand: String = "",
    val category: String = "",
    val color: String = "",
    val season: String = "",
    @ServerTimestamp
    val createdAt: Timestamp? = null
)