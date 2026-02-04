package com.drape.data.model

import com.google.firebase.firestore.DocumentId

/**
 * Represents a planned outfit for a specific day.
 * 
 * @property date Date in "yyyy-MM-dd" format (e.g., "2026-02-03")
 */
data class PlannedDay(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val date: String = "",
    val items: List<PlannedItem> = emptyList()
)

/**
 * Represents an item in a planned outfit.
 */
data class PlannedItem(
    val label: String = "Daily",
    val outfitId: String = "",
)
