package com.drape.data.repository

import android.net.Uri
import com.drape.data.datasource.StorageRemoteDataSource
import com.drape.data.model.ClothingItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing clothing items.
 * Coordinates between Firebase Storage (for images) and Firestore (for metadata).
 */
@Singleton
class ClothesRepository @Inject constructor(
    private val storageDataSource: StorageRemoteDataSource,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val clothesCollection = firestore.collection("clothes")

    /**
     * Uploads a new clothing item with image and metadata.
     * 
     * @param imageUri The local URI of the clothing image
     * @param clothingData The clothing item metadata (without id, userId, imageUrl, createdAt)
     * @return Complete ClothingItem with generated fields
     * @throws Exception If upload fails
     */
    suspend fun uploadClothingItem(
        imageUri: Uri,
        clothingData: ClothingItem
    ): ClothingItem {
        val currentUserId = auth.currentUser?.uid
            ?: throw Exception("User not authenticated")

        return try {
            // Generate unique ID
            val clothingId = storageDataSource.generateClothingId()
            
            // Upload image to Firebase Storage
            val imageUrl = storageDataSource.uploadImage(imageUri, currentUserId, clothingId)
            
            // Create complete clothing item
            val completeItem = clothingData.copy(
                id = clothingId,
                userId = currentUserId,
                imageUrl = imageUrl,
                createdAt = System.currentTimeMillis()
            )
            
            // Save metadata to Firestore
            clothesCollection.document(clothingId).set(completeItem).await()
            
            completeItem
        } catch (e: Exception) {
            throw Exception("Failed to upload clothing item: ${e.message}", e)
        }
    }

    /**
     * Deletes a clothing item (both image and metadata).
     * 
     * @param clothingId The ID of the clothing item to delete
     * @return true if deletion was successful, false otherwise
     */
    suspend fun deleteClothingItem(clothingId: String): Boolean {
        return try {
            val currentUserId = auth.currentUser?.uid
                ?: throw Exception("User not authenticated")

            // Get clothing item data
            val document = clothesCollection.document(clothingId).get().await()
            if (!document.exists()) return false

            val clothingItem = document.toObject(ClothingItem::class.java)
                ?: return false

            // Verify ownership
            if (clothingItem.userId != currentUserId) {
                throw Exception("Unauthorized to delete this item")
            }

            // Delete image from Firebase Storage
            val imagePath = storageDataSource.generateImagePath(currentUserId, clothingId)
            storageDataSource.deleteImage(imagePath)

            // Delete metadata from Firestore
            clothesCollection.document(clothingId).delete().await()

            true
        } catch (e: Exception) {
            throw Exception("Failed to delete clothing item: ${e.message}", e)
        }
    }

    /**
     * Gets all clothing items for the current user.
     * 
     * @return Flow of clothing items list
     */
    fun getUserClothingItems(): Flow<List<ClothingItem>> = callbackFlow {
        val currentUserId = auth.currentUser?.uid
            ?: throw Exception("User not authenticated")

        val listener = clothesCollection
            .whereEqualTo("userId", currentUserId)
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

    /**
     * Gets a specific clothing item by ID.
     * 
     * @param clothingId The ID of the clothing item
     * @return ClothingItem if found, null otherwise
     */
    suspend fun getClothingItem(clothingId: String): ClothingItem? {
        return try {
            val currentUserId = auth.currentUser?.uid
                ?: throw Exception("User not authenticated")

            val document = clothesCollection.document(clothingId).get().await()
            val clothingItem = document.toObject(ClothingItem::class.java)

            // Verify ownership
            if (clothingItem?.userId == currentUserId) {
                clothingItem
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}