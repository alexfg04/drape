package com.drape.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

/**
 * Categories for clothing items to prevent typos and ensure consistency.
 */
enum class ItemCategory {
    TOP, BOTTOM, SHOES, ACCESSORIES
}

/**
 * Represents a single clothing item placed and transformed within the outfit editor.
 */
data class PlacedItem(
    val itemId: String = "",       // ID of the item in the general catalog
    val category: ItemCategory = ItemCategory.TOP,
    val posX: Float = 0f,          // X coordinate in the editor
    val posY: Float = 0f,          // Y coordinate in the editor
    val scale: Float = 1f,         // Scale factor (for resizing)
    val rotation: Float = 0f,      // Rotation in degrees
    val zIndex: Int? = 0            // Stacking order (higher values are on top)
)


/**
 * Data model for a complete Outfit, containing metadata and a list of placed items.
 */
data class Outfit(
    @DocumentId
    val id: String = "",           // Automatically populated by Firebase
    val userId: String = "",       // Used to filter outfits for a specific user
    val name: String = "New Outfit",
    val thumbnailUrl: String = "", // URL of the thumbnail image stored in Firebase Storage
    val items: List<PlacedItem> = emptyList(),

    @ServerTimestamp
    val createdAt: Timestamp? = null    // Server-side creation timestamp
)
