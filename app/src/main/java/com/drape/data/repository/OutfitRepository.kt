package com.drape.data.repository

import android.net.Uri
import com.drape.data.datasource.OutfitsRemoteDataSource
import com.drape.data.datasource.StorageRemoteDataSource
import com.drape.data.model.Outfit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing outfits.
 * Coordinates between image storage and metadata storage.
 */
@Singleton
class OutfitRepository @Inject constructor(
    private val outfitsRemoteDataSource: OutfitsRemoteDataSource,
    private val storageDataSource: StorageRemoteDataSource,
    private val authRepository: AuthRepository
) {
    /**
     * Saves or updates an outfit, including its thumbnail image if provided.
     *
     * @param outfit The [Outfit] data to save.
     * @param thumbnailUri The optional local URI for the outfit's thumbnail image.
     * @throws Exception If the user is not authenticated or the save operation fails.
     */
    suspend fun saveOutfit(outfit: Outfit, thumbnailUri: Uri? = null) {
        val currentUserId = authRepository.currentUser?.id
            ?: throw Exception("User not authenticated")

        val outfitId = outfit.id.ifEmpty {
            storageDataSource.generateId()
        }

        try {
            var thumbnailUrl = outfit.thumbnailUrl
            
            // Upload thumbnail if provided
            if (thumbnailUri != null) {
                thumbnailUrl = storageDataSource.uploadImage(
                    imageUri = thumbnailUri,
                    userId = currentUserId,
                    id = outfitId,
                    folder = "outfits"
                )
            }

            val outfitToSave = outfit.copy(
                id = outfitId,
                userId = currentUserId,
                thumbnailUrl = thumbnailUrl
            )

            outfitsRemoteDataSource.saveOutfit(outfitToSave)
        } catch (e: Exception) {
            throw Exception("Failed to save outfit: ${e.message}", e)
        }
    }

    /**
     * Deletes an outfit and its associated resources.
     * 
     * @param outfitId The ID of the outfit to delete
     * @return true if deletion was successful
     */
    suspend fun deleteOutfit(outfitId: String): Boolean {
        return try {
            val currentUserId = authRepository.currentUser?.id
                ?: throw Exception("User not authenticated")

            val outfit = outfitsRemoteDataSource.getOutfit(outfitId)
                ?: return false

            if (outfit.userId != currentUserId) {
                throw Exception("Unauthorized to delete this outfit")
            }

            // If there's a thumbnail in storage, delete it
            if (outfit.thumbnailUrl.isNotEmpty() && outfit.thumbnailUrl.contains("firebasestorage")) {
                try {
                    val path = storageDataSource.extractPathFromUrl(outfit.thumbnailUrl)
                    storageDataSource.deleteImage(path)
                } catch (e: Exception) {
                    // Log error but proceed with metadata deletion
                }
            }

            outfitsRemoteDataSource.deleteOutfit(outfitId)
            true
        } catch (e: Exception) {
            throw Exception("Failed to delete outfit: ${e.message}", e)
        }
    }

    /**
     * Gets all outfits for the current user.
     *
     * @return A [Flow] emitting a list of [Outfit]s belonging to the authenticated user.
     */
    fun getUserOutfits(): Flow<List<Outfit>> {
        val userId = authRepository.currentUser?.id
            ?: return emptyFlow()
            
        return outfitsRemoteDataSource.getUserOutfits(userId)
    }

    /**
     * Gets a specific outfit by ID, ensuring it belongs to the current user.
     *
     * @param outfitId The ID of the outfit.
     * @return The [Outfit] if found and authorized, null otherwise.
     * @throws Exception If the user is not authenticated.
     */
    suspend fun getOutfit(outfitId: String): Outfit? {
        val currentUserId = authRepository.currentUser?.id
            ?: throw Exception("User not authenticated")

        val outfit = outfitsRemoteDataSource.getOutfit(outfitId)
        
        return if (outfit?.userId == currentUserId) outfit else null
    }
}
