package com.drape.data.datasource

import com.drape.data.model.ClothingItem
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Remote data source for Firestore operations related to clothing items.
 */
@Singleton
class ClothesRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val clothesCollection = firestore.collection("clothes")

    /**
     * Saves a clothing item to Firestore.
     */
    suspend fun saveClothingItem(clothingItem: ClothingItem) {
        clothesCollection.document(clothingItem.id).set(clothingItem).await()
    }

    /**
     * Deletes a clothing item from Firestore.
     */
    suspend fun deleteClothingItem(clothingId: String) {
        clothesCollection.document(clothingId).delete().await()
    }

    /**
     * Gets a specific clothing item from Firestore.
     */
    suspend fun getClothingItem(clothingId: String): ClothingItem? {
        val document = clothesCollection.document(clothingId).get().await()
        return if (document.exists()) {
            document.toObject(ClothingItem::class.java)
        } else {
            null
        }
    }

    /**
     * Returns a Flow of clothing items for a specific user.
     */
    fun getUserClothingItems(userId: String): Flow<List<ClothingItem>> = callbackFlow {
        val listener = clothesCollection
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val items = snapshot?.documents?.mapNotNull { document ->
                    document.toObject(ClothingItem::class.java)
                } ?: emptyList()

                trySend(items)
            }

        awaitClose { listener.remove() }
    }
}
