package com.drape.data.repository

import com.drape.data.datasource.PlannedDaysRemoteDataSource
import com.drape.data.model.PlannedDay
import com.drape.data.model.PlannedItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
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
     * Adds an outfit to a specific day. Creates the day if it doesn't exist.
     *
     * @param date The date in "yyyy-MM-dd" format
     * @param outfitId The ID of the outfit to add
     * @param label Optional label for the outfit (e.g., "Morning", "Evening")
     */
    suspend fun addOutfitToDay(date: String, outfitId: String, label: String = "Daily") {
        val currentUserId = authRepository.currentUser?.id
            ?: throw Exception("User not authenticated")

        val existingDay = plannedDaysRemoteDataSource.getPlannedDayByDate(currentUserId, date)
        
        val newItem = PlannedItem(label = label, outfitId = outfitId)
        val updatedItems = (existingDay?.items ?: emptyList()) + newItem

        savePlannedDay(date, updatedItems)
    }

    /**
     * Removes an outfit from a specific day.
     *
     * @param date The date in "yyyy-MM-dd" format
     * @param outfitId The ID of the outfit to remove
     */
    suspend fun removeOutfitFromDay(date: String, outfitId: String) {
        val currentUserId = authRepository.currentUser?.id
            ?: throw Exception("User not authenticated")

        val existingDay = plannedDaysRemoteDataSource.getPlannedDayByDate(currentUserId, date)
            ?: return

        val updatedItems = existingDay.items.filterNot { it.outfitId == outfitId }
        
        if (updatedItems.isEmpty()) {
            plannedDaysRemoteDataSource.deletePlannedDay(existingDay.id)
        } else {
            savePlannedDay(date, updatedItems)
        }
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
     *
     * @return A Flow emitting a list of PlannedDays
     */
    fun getAllPlannedDays(): Flow<List<PlannedDay>> {
        val userId = authRepository.currentUser?.id
            ?: return emptyFlow()

        return plannedDaysRemoteDataSource.getUserPlannedDays(userId)
    }

    /**
     * Gets planned days within a date range.
     *
     * @param startDate Start date in "yyyy-MM-dd" format (inclusive)
     * @param endDate End date in "yyyy-MM-dd" format (inclusive)
     * @return A Flow emitting planned days in the range
     */
    fun getPlannedDaysInRange(startDate: String, endDate: String): Flow<List<PlannedDay>> {
        val userId = authRepository.currentUser?.id
            ?: return emptyFlow()

        return plannedDaysRemoteDataSource.getUserPlannedDaysInRange(userId, startDate, endDate)
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
