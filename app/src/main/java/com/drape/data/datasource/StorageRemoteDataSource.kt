package com.drape.data.datasource

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Remote data source for Firebase Storage operations.
 * Handles upload and deletion of clothing item images.
 */
@Singleton
class StorageRemoteDataSource @Inject constructor(
    private val storage: FirebaseStorage
) {
    /**
     * Uploads an image to Firebase Storage.
     *
     * @param imageUri Local URI of the image
     * @param userId User ID for path organization
     * @param clothingId Unique clothing item ID
     * @return Download URL of the uploaded image
     */
    suspend fun uploadImage(
        imageUri: Uri,
        userId: String,
        clothingId: String
    ): String {
        val imagePath = generateImagePath(userId, clothingId)
        val imageRef = storage.reference.child(imagePath)
        
        imageRef.putFile(imageUri).await()
        return imageRef.downloadUrl.await().toString()
    }
    
    /**
     * Deletes an image from Firebase Storage.
     *
     * @param imagePath Full path to the image in storage
     */
    suspend fun deleteImage(imagePath: String) {
        storage.reference.child(imagePath).delete().await()
    }
    
    /**
     * Generates a unique ID for a clothing item.
     */
    fun generateClothingId(): String {
        return UUID.randomUUID().toString()
    }
    
    /**
     * Generates the storage path for a clothing image.
     *
     * @param userId User ID
     * @param clothingId Clothing item ID
     * @return Path in format "users/{userId}/clothes/{clothingId}.jpg"
     */
    fun generateImagePath(userId: String, clothingId: String): String {
        return "users/$userId/clothes/$clothingId.jpg"
    }
}
