package com.drape.data.repository

import com.drape.data.datasource.PlannedDaysRemoteDataSource
import com.drape.data.model.PlannedDay
import com.drape.data.model.PlannedItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing planned days (outfit calendar).
 * Uses date strings in "yyyy-MM-dd" format for timezone-safe storage.
 */
@Singleton
class PlannedDaysRepository @Inject constructor(
    private val plannedDaysRemoteDataSource: PlannedDaysRemoteDataSource,
    private val authRepository: AuthRepository
) {
    /**
     * Saves or updates a planned day.
     *
     * @param date The date in "yyyy-MM-dd" format
     * @param items The list of planned items (outfits) for that day
     * @throws Exception If the user is not authenticated
     */
    suspend fun savePlannedDay(date: String, items: List<PlannedItem>) {
        val currentUserId = authRepository.currentUser?.id
            ?: throw Exception("User not authenticated")

        val plannedDay = PlannedDay(
            id = "${currentUserId}_$date",
            userId = currentUserId,
            date = date,
            items = items
        )

        plannedDaysRemoteDataSource.savePlannedDay(plannedDay)
    }

    /**
     * Atomically adds an outfit to a specific day using Firestore transactions.
     * Creates the day if it doesn't exist.
     *
     * @param date The date in "yyyy-MM-dd" format
     * @param outfitId The ID of the outfit to add
     * @param label Optional label for the outfit (e.g., "Morning", "Evening")
     * @throws Exception If the user is not authenticated
     */
    suspend fun addOutfitToDay(date: String, outfitId: String, label: String = "Daily") {
        val currentUserId = authRepository.currentUser?.id
            ?: throw Exception("User not authenticated")

        val newItem = PlannedItem(label = label, outfitId = outfitId)
        plannedDaysRemoteDataSource.addOutfitToDay(currentUserId, date, newItem)
    }

    /**
     * Atomically removes an outfit from a specific day using Firestore transactions.
     * Deletes the document if the resulting items list is empty.
     *
     * @param date The date in "yyyy-MM-dd" format
     * @param outfitId The ID of the outfit to remove
     * @throws Exception If the user is not authenticated
     */
    suspend fun removeOutfitFromDay(date: String, outfitId: String) {
        val currentUserId = authRepository.currentUser?.id
            ?: throw Exception("User not authenticated")

        plannedDaysRemoteDataSource.removeOutfitFromDay(currentUserId, date, outfitId)
    }

    /**
     * Deletes a planned day entirely.
     *
     * @param date The date in "yyyy-MM-dd" format
     */
    suspend fun deletePlannedDay(date: String) {
        val currentUserId = authRepository.currentUser?.id
            ?: throw Exception("User not authenticated")

        val documentId = "${currentUserId}_$date"
        plannedDaysRemoteDataSource.deletePlannedDay(documentId)
    }

    /**
     * Gets the planned day for a specific date.
     *
     * @param date The date in "yyyy-MM-dd" format
     * @return The PlannedDay if found, null otherwise
     */
    suspend fun getPlannedDay(date: String): PlannedDay? {
        val currentUserId = authRepository.currentUser?.id
            ?: return null

        return plannedDaysRemoteDataSource.getPlannedDayByDate(currentUserId, date)
    }

    /**
     * Gets all planned days for the current user.
     * Emits an empty list if user is not authenticated.
     *
     * @return A Flow emitting a list of PlannedDays
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun getAllPlannedDays(): Flow<List<PlannedDay>> {
        return authRepository.currentUserIdFlow
            .flatMapLatest { userId ->
                if (userId.isNullOrBlank()) {
                    flowOf(emptyList())
                } else {
                    plannedDaysRemoteDataSource.getUserPlannedDays(userId)
                }
            }
            .catch { emit(emptyList()) }
    }

    /**
     * Gets planned days within a date range.
     * Emits an empty list if user is not authenticated.
     *
     * @param startDate Start date in "yyyy-MM-dd" format (inclusive)
     * @param endDate End date in "yyyy-MM-dd" format (inclusive)
     * @return A Flow emitting planned days in the range
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun getPlannedDaysInRange(startDate: String, endDate: String): Flow<List<PlannedDay>> {
        return authRepository.currentUserIdFlow
            .flatMapLatest { userId ->
                if (userId.isNullOrBlank()) {
                    flowOf(emptyList())
                } else {
                    plannedDaysRemoteDataSource.getUserPlannedDaysInRange(userId, startDate, endDate)
                }
            }
            .catch { emit(emptyList()) }
    }

    /**
     * Counts how many times each outfit has been used (planned).
     * Useful for statistics and recommendations.
     *
     * @param plannedDays List of planned days to analyze
     * @return Map of outfitId to usage count
     */
    fun countOutfitUsage(plannedDays: List<PlannedDay>): Map<String, Int> {
        return plannedDays
            .flatMap { it.items }
            .groupingBy { it.outfitId }
            .eachCount()
    }
}
