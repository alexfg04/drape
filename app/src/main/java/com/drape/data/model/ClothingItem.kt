package com.drape.data.model

/**
 * Data model representing a clothing item in the user's wardrobe.
 */
data class ClothingItem(
    val id: String = "",
    val userId: String = "",
    val imageUrl: String = "",
    val name: String = "",
    val brand: String = "",
    val category: String = "",
    val color: String = "",
    val season: String = "",
    val createdAt: Long = 0L
)