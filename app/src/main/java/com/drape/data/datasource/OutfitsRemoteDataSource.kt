package com.drape.data.datasource

import com.drape.data.model.Outfit
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Remote data source for Firestore operations related to outfits.
 */
@Singleton
class OutfitsRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val outfitsCollection = firestore.collection("outfits")

    /**
     * Saves an outfit to Firestore.
     * Uses Firestore's automatic ID generation if the outfit ID is empty.
     */
    suspend fun saveOutfit(outfit: Outfit) {
        val documentRef = if (outfit.id.isEmpty()) {
            outfitsCollection.document()
        } else {
            outfitsCollection.document(outfit.id)
        }
        documentRef.set(outfit).await()
    }




    /**
     * Deletes an outfit from Firestore.
     */
    suspend fun deleteOutfit(outfitId: String) {
        outfitsCollection.document(outfitId).delete().await()
    }

    /**
     * Gets a specific outfit from Firestore.
     */
    suspend fun getOutfit(outfitId: String): Outfit? {
        val document = outfitsCollection.document(outfitId).get().await()
        return if (document.exists()) {
            document.toObject(Outfit::class.java)
        } else {
            null
        }
    }

    /**
     * Returns a Flow of outfits for a specific user.
     */
    fun getUserOutfits(userId: String): Flow<List<Outfit>> = callbackFlow {
        val listener = outfitsCollection
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val items = snapshot?.documents?.mapNotNull { document ->
                    document.toObject(Outfit::class.java)
                } ?: emptyList()

                trySend(items)
            }

        awaitClose { listener.remove() }
    }
}
