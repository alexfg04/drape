package com.drape.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

/**
 * Data model representing a clothing item in the user's wardrobe.
 *
 * @property id Unique identifier for the item (Firestore Document ID).
 * @property userId ID of the user who owns this item.
 * @property imageUrl Remote URL of the stored image.
 * @property name User-provided name for the item.
 * @property brand Brand of the item.
 * @property category Category of the clothing (e.g., TOP, BOTTOM).
 * @property color Primary color of the item.
 * @property season Recommended season for the item.
 * @property createdAt Server-side timestamp of when the item was saved.
 */
data class ClothingItem(
    @DocumentId
    var id: String = "",
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