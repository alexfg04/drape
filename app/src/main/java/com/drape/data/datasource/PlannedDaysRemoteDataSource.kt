package com.drape.data.datasource

import com.drape.data.model.PlannedDay
import com.drape.data.model.PlannedItem
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Remote data source for Firestore operations related to planned days.
 * Uses date strings in "yyyy-MM-dd" format for timezone-safe storage.
 */
@Singleton
class PlannedDaysRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val plannedDaysCollection = firestore.collection("plannedDays")

    /**
     * Saves a planned day to Firestore.
     * Uses "{userId}_{date}" as document ID for easy querying.
     * 
     * @throws IllegalArgumentException if userId or date is blank
     */
    suspend fun savePlannedDay(plannedDay: PlannedDay) {
        require(plannedDay.userId.isNotBlank()) {
            "Cannot save PlannedDay to $plannedDaysCollection: userId is blank"
        }
        require(plannedDay.date.isNotBlank()) {
            "Cannot save PlannedDay to $plannedDaysCollection: date is blank"
        }
        
        val documentId = plannedDay.id.ifBlank {
            "${plannedDay.userId}_${plannedDay.date}"
        }
        
        val plannedDayWithId = plannedDay.copy(id = documentId)
        plannedDaysCollection.document(documentId).set(plannedDayWithId).await()
    }

    /**
     * Atomically adds an outfit to a day using FieldValue.arrayUnion.
     * Creates the document if it doesn't exist.
     * 
     * @throws IllegalArgumentException if userId or date is blank
     */
    suspend fun addOutfitToDay(userId: String, date: String, item: PlannedItem) {
        require(userId.isNotBlank()) {
            "Cannot add outfit to $plannedDaysCollection: userId is blank"
        }
        require(date.isNotBlank()) {
            "Cannot add outfit to $plannedDaysCollection: date is blank"
        }
        
        val documentId = "${userId}_${date}"
        val docRef = plannedDaysCollection.document(documentId)
        
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(docRef)
            if (snapshot.exists()) {
                transaction.update(docRef, "items", FieldValue.arrayUnion(item))
            } else {
                val newDay = PlannedDay(
                    id = documentId,
                    userId = userId,
                    date = date,
                    items = listOf(item)
                )
                transaction.set(docRef, newDay)
            }
        }.await()
    }

    /**
     * Atomically removes an outfit from a day.
     * Deletes the document if the resulting items list is empty.
     * 
     * @throws IllegalArgumentException if userId or date is blank
     */
    suspend fun removeOutfitFromDay(userId: String, date: String, outfitId: String) {
        require(userId.isNotBlank()) {
            "Cannot remove outfit from $plannedDaysCollection: userId is blank"
        }
        require(date.isNotBlank()) {
            "Cannot remove outfit from $plannedDaysCollection: date is blank"
        }
        
        val documentId = "${userId}_${date}"
        val docRef = plannedDaysCollection.document(documentId)
        
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(docRef)
            if (snapshot.exists()) {
                val plannedDay = snapshot.toObject(PlannedDay::class.java)
                val updatedItems = plannedDay?.items?.filterNot { it.outfitId == outfitId } ?: emptyList()
                
                if (updatedItems.isEmpty()) {
                    transaction.delete(docRef)
                } else {
                    transaction.update(docRef, "items", updatedItems)
                }
            }
        }.await()
    }

    /**
     * Deletes a planned day from Firestore.
     */
    suspend fun deletePlannedDay(plannedDayId: String) {
        plannedDaysCollection.document(plannedDayId).delete().await()
    }

    /**
     * Gets a specific planned day from Firestore.
     */
    suspend fun getPlannedDay(plannedDayId: String): PlannedDay? {
        val document = plannedDaysCollection.document(plannedDayId).get().await()
        return if (document.exists()) {
            document.toObject(PlannedDay::class.java)
        } else {
            null
        }
    }

    /**
     * Gets a planned day for a specific user and date.
     * 
     * @param userId The user ID
     * @param date Date in "yyyy-MM-dd" format
     */
    suspend fun getPlannedDayByDate(userId: String, date: String): PlannedDay? {
        val documentId = "${userId}_${date}"
        return getPlannedDay(documentId)
    }

    /**
     * Returns a Flow of all planned days for a specific user.
     */
    fun getUserPlannedDays(userId: String): Flow<List<PlannedDay>> = callbackFlow {
        val listener = plannedDaysCollection
            .whereEqualTo("userId", userId)
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val items = snapshot?.documents?.mapNotNull { document ->
                    document.toObject(PlannedDay::class.java)
                } ?: emptyList()

                trySend(items)
            }

        awaitClose { listener.remove() }
    }

    /**
     * Returns a Flow of planned days within a date range for a specific user.
     * 
     * @param userId The user ID
     * @param startDate Start date in "yyyy-MM-dd" format (inclusive)
     * @param endDate End date in "yyyy-MM-dd" format (inclusive)
     */
    fun getUserPlannedDaysInRange(
        userId: String,
        startDate: String,
        endDate: String
    ): Flow<List<PlannedDay>> = callbackFlow {
        val listener = plannedDaysCollection
            .whereEqualTo("userId", userId)
            .whereGreaterThanOrEqualTo("date", startDate)
            .whereLessThanOrEqualTo("date", endDate)
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val items = snapshot?.documents?.mapNotNull { document ->
                    document.toObject(PlannedDay::class.java)
                } ?: emptyList()

                trySend(items)
            }

        awaitClose { listener.remove() }
    }
}
