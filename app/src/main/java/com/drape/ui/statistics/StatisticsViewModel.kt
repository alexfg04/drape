package com.drape.ui.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drape.data.model.ClothingItem
import com.drape.data.model.Outfit
import com.drape.data.model.PlannedDay
import com.drape.data.repository.ClothesRepository
import com.drape.data.repository.OutfitRepository
import com.drape.data.repository.PlannedDaysRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import okhttp3.internal.userAgent
import javax.inject.Inject

/**
 * Data class representing outfit usage statistics.
 *
 * @property totalOutfits Total number of outfits in the wardrobe
 * @property usedOutfits Number of outfits that have been used in the planner
 * @property unusedOutfits Number of outfits never used in the planner
 * @property usagePercentage Percentage of outfits used (0-100)
 */
data class OutfitStats(
    val totalOutfits: Int,
    val usedOutfits: Int,
    val unusedOutfits: Int,
    val usagePercentage: Float
) {
    companion object {
        /** Empty state with zero values */
        val EMPTY = OutfitStats(0, 0, 0, 0f)
    }
}

/**
 * Data class representing clothing items usage statistics.
 *
 * @property totalClothes Total number of clothing items in the wardrobe
 * @property usedClothes Number of clothing items used in planned outfits
 * @property unusedClothes Number of clothing items never used
 * @property usagePercentage Percentage of clothes used (0-100)
 * @property byCategory Distribution of clothing items by category (e.g., "TOP" -> 5)
 * @property byColor Distribution of clothing items by color (e.g., "Red" -> 3)
 */
data class ClothingStats(
    val totalClothes: Int,
    val usedClothes: Int,
    val unusedClothes: Int,
    val usagePercentage: Float,
    val byCategory: Map<String, Int>,
    val byColor: Map<String, Int>
) {
    companion object {
        /** Empty state with zero values and empty distributions */
        val EMPTY = ClothingStats(0, 0, 0, 0f, emptyMap(), emptyMap())
    }
}

/**
 * Data class representing monthly usage statistics for chart visualization.
 *
 * @property month Month identifier in "yyyy-MM" format
 * @property outfitCount Number of outfits scheduled in this month
 * @property clothesCount Number of clothing items used in this month (currently simplified)
 */
data class MonthlyUsageStats(
    val month: String,
    val outfitCount: Int,
    val clothesCount: Int
)

/**
 * Data class representing the most frequently used outfits.
 *
 * @property outfitId Unique identifier of the outfit
 * @property outfitName Display name of the outfit
 * @property usageCount Number of times this outfit has been scheduled
 * @property thumbnailUrl URL of the outfit thumbnail image (nullable)
 */
data class TopUsedOutfit(
    val outfitId: String,
    val outfitName: String,
    val usageCount: Int,
    val thumbnailUrl: String?
)

data class LeastUsedOutfit(
    val outfitId: String,
    val outfitName: String,
    val usageCount: Int,
    val thumbnailUrl: String?
)


/**
 * Combined UI state for the statistics screen.
 * Contains all statistical data and loading state.
 *
 * @property outfitStats Statistics about outfits usage
 * @property clothingStats Statistics about clothing items usage
 * @property monthlyStats Monthly usage data for charts
 * @property topUsedOutfits List of most frequently used outfits
 * @property isLoading Whether the statistics are currently being calculated
 */
data class StatisticsUiState(
    val outfitStats: OutfitStats = OutfitStats.EMPTY,
    val clothingStats: ClothingStats = ClothingStats.EMPTY,
    val monthlyStats: List<MonthlyUsageStats> = emptyList(),
    val topUsedOutfits: List<TopUsedOutfit> = emptyList(),
    val leastUsedOutfits: List<LeastUsedOutfit> = emptyList(),
    val isLoading: Boolean = true
)

/**
 * ViewModel responsible for calculating and providing wardrobe statistics.
 * 
 * This ViewModel aggregates data from outfits, clothing items, and planned days
 * to calculate various usage statistics including:
 * - Outfit usage percentage (used vs total)
 * - Clothing items usage percentage
 * - Distribution by category and color
 * - Monthly usage trends
 * - Top 5 most used outfits
 *
 * @param outfitRepository Repository for accessing outfit data
 * @param clothesRepository Repository for accessing clothing items data
 * @param plannedDaysRepository Repository for accessing planner data
 */
@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val outfitRepository: OutfitRepository,
    private val clothesRepository: ClothesRepository,
    private val plannedDaysRepository: PlannedDaysRepository
) : ViewModel() {

    private val outfitsFlow: Flow<List<Outfit>> = outfitRepository.getUserOutfits()
    private val clothesFlow: Flow<List<ClothingItem>> = clothesRepository.getUserClothingItems()
    private val plannedDaysFlow: Flow<List<PlannedDay>> = plannedDaysRepository.getAllPlannedDays()

    /**
     * Combined UI state flow that automatically updates when underlying data changes.
     * Uses [SharingStarted.WhileSubscribed] to keep the flow active while there are subscribers.
     */
    val uiState: StateFlow<StatisticsUiState> = combine(
        outfitsFlow,
        clothesFlow,
        plannedDaysFlow
    ) { outfits, clothes, plannedDays ->
        val outfitStats = calculateOutfitStats(outfits, plannedDays)
        val clothingStats = calculateClothingStats(clothes, outfits, plannedDays)
        val monthlyStats = calculateMonthlyStats(plannedDays)
        val topUsedOutfits = calculateTopUsedOutfits(outfits, plannedDays)
        val leastUsedOutfits = calculateLeastUsedOutfits(outfits, plannedDays)


        StatisticsUiState(
            outfitStats = outfitStats,
            clothingStats = clothingStats,
            monthlyStats = monthlyStats,
            topUsedOutfits = topUsedOutfits,
            leastUsedOutfits = leastUsedOutfits,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StatisticsUiState(isLoading = true)
    )

    /**
     * Calculates outfit usage statistics.
     *
     * An outfit is considered "used" if it appears in any planned day.
     *
     * @param outfits List of all outfits in the wardrobe
     * @param plannedDays List of all planned days with scheduled outfits
     * @return [OutfitStats] containing calculated statistics
     */
    private fun calculateOutfitStats(
        outfits: List<Outfit>,
        plannedDays: List<PlannedDay>
    ): OutfitStats {
        val totalOutfits = outfits.size

        // Get valid outfit IDs from current outfits list
        val validOutfitIds = outfits.map { it.id }.toSet()

        // Get all outfit IDs that have been used in planner, intersected with valid IDs
        val plannedOutfitIds = plannedDays
            .flatMap { it.items }
            .map { it.outfitId }
            .toSet()

        // Only count outfits that both appear in planner AND still exist in wardrobe
        val usedOutfits = plannedOutfitIds.intersect(validOutfitIds).size
        val unusedOutfits = (totalOutfits - usedOutfits).coerceAtLeast(0)
        val usagePercentage = if (totalOutfits > 0) {
            ((usedOutfits.toFloat().coerceIn(0f, totalOutfits.toFloat()) / totalOutfits) * 100)
                .coerceIn(0f, 100f)
        } else 0f

        return OutfitStats(
            totalOutfits = totalOutfits,
            usedOutfits = usedOutfits,
            unusedOutfits = unusedOutfits,
            usagePercentage = usagePercentage
        )
    }

    /**
     * Calculates clothing items usage statistics.
     *
     * A clothing item is considered "used" if it belongs to an outfit
     * that has been scheduled in the planner.
     *
     * @param clothes List of all clothing items in the wardrobe
     * @param outfits List of all outfits
     * @param plannedDays List of all planned days
     * @return [ClothingStats] containing calculated statistics and distributions
     */
    private fun calculateClothingStats(
        clothes: List<ClothingItem>,
        outfits: List<Outfit>,
        plannedDays: List<PlannedDay>
    ): ClothingStats {
        val totalClothes = clothes.size

        // Get valid clothing IDs from current clothes list
        val existingClothingIds = clothes.map { it.id }.toSet()

        // Get outfit IDs that have been used
        val usedOutfitIds = plannedDays
            .flatMap { it.items }
            .map { it.outfitId }
            .toSet()

        // Get clothing IDs used in planned outfits, intersected with existing IDs
        val plannedClothingIds = outfits
            .filter { it.id in usedOutfitIds }
            .flatMap { it.items }
            .map { it.itemId }
            .toSet()

        // Only count clothing items that both appear in planned outfits AND still exist in wardrobe
        val usedClothingIds = plannedClothingIds.intersect(existingClothingIds)
        val usedClothes = usedClothingIds.size
        val unusedClothes = (totalClothes - usedClothes).coerceAtLeast(0)
        val usagePercentage = if (totalClothes > 0) {
            ((usedClothes.toFloat().coerceIn(0f, totalClothes.toFloat()) / totalClothes) * 100)
                .coerceIn(0f, 100f)
        } else 0f

        // Group by category
        val byCategory = clothes.groupingBy { it.category }.eachCount()

        // Group by color
        val byColor = clothes.groupingBy { it.color }.eachCount()

        return ClothingStats(
            totalClothes = totalClothes,
            usedClothes = usedClothes,
            unusedClothes = unusedClothes,
            usagePercentage = usagePercentage,
            byCategory = byCategory,
            byColor = byColor
        )
    }

    /**
     * Calculates monthly usage statistics for chart visualization.
     *
     * Groups planned days by month and counts outfits scheduled in each month.
     *
     * @param plannedDays List of all planned days
     * @return List of [MonthlyUsageStats] sorted chronologically
     */
    private fun calculateMonthlyStats(
        plannedDays: List<PlannedDay>
    ): List<MonthlyUsageStats> {
        // Group planned days by month
        val monthlyData = plannedDays
            .groupBy { plannedDay ->
                // Extract year-month from date (yyyy-MM-dd format)
                plannedDay.date.take(7) // Gets "yyyy-MM"
            }
            .map { (month, days) ->
                val outfitCount = days.sumOf { it.items.size }
                MonthlyUsageStats(
                    month = month,
                    outfitCount = outfitCount,
                    clothesCount = 0 // Simplified - would need to count actual clothes per outfit
                )
            }
            .sortedBy { it.month }

        return monthlyData
    }

    /**
     * Calculates the top 5 most frequently used outfits.
     *
     * Counts how many times each outfit appears in planned days
     * and returns the top 5 sorted by usage count.
     *
     * @param outfits List of all outfits
     * @param plannedDays List of all planned days
     * @return List of [TopUsedOutfit] sorted by usage count (descending), limited to 5 items
     */
    private fun calculateTopUsedOutfits(
        outfits: List<Outfit>,
        plannedDays: List<PlannedDay>
    ): List<TopUsedOutfit> {
        // Count usage per outfit
        val usageCounts = plannedDays
            .flatMap { it.items }
            .groupingBy { it.outfitId }
            .eachCount()

        // Map to TopUsedOutfit, sorted by usage
        return usageCounts
            .map { (outfitId, count) ->
                val outfit = outfits.find { it.id == outfitId }
                TopUsedOutfit(
                    outfitId = outfitId,
                    outfitName = outfit?.name ?: "Outfit sconosciuto",
                    usageCount = count,
                    thumbnailUrl = outfit?.thumbnailUrl
                )
            }
            .sortedByDescending { it.usageCount }
            .take(5) // Top 5
    }

    private fun calculateLeastUsedOutfits(
        outfits: List<Outfit>,
        plannedDays: List<PlannedDay>
    ): List<LeastUsedOutfit> {
        // usage per outfit
        val usageCounts = plannedDays
            .flatMap { it.items }
            .groupingBy { it.outfitId }
            .eachCount()

        // map to LeastUsedOutfit, sorted by usage
        return outfits
            .map { outfit ->
                LeastUsedOutfit(
                    outfitId = outfit.id,
                    outfitName = outfit.name,
                    usageCount = usageCounts[outfit.id] ?: 0,
                    thumbnailUrl = outfit.thumbnailUrl
                )
            }
            .sortedWith(compareBy<LeastUsedOutfit> { it.usageCount }
                .thenBy { it.outfitName })
            .take(5) //  5
    }
}
