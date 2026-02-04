package com.drape.data.datasource

import com.drape.data.model.PlannedDay
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
     */
    suspend fun savePlannedDay(plannedDay: PlannedDay) {
        val documentId = plannedDay.id.ifBlank {
            "${plannedDay.userId}_${plannedDay.date}"
        }
        
        val plannedDayWithId = plannedDay.copy(id = documentId)
        plannedDaysCollection.document(documentId).set(plannedDayWithId).await()
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
