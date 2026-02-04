package com.drape.ui.planner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drape.data.model.Outfit
import com.drape.data.model.PlannedDay
import com.drape.data.repository.OutfitRepository
import com.drape.data.repository.PlannedDaysRepository
import com.drape.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class PlannerViewModel @Inject constructor(
    private val plannedDaysRepository: PlannedDaysRepository,
    private val outfitRepository: OutfitRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(PlannerUiState())
    val uiState: StateFlow<PlannerUiState> = _uiState.asStateFlow()

    init {
        loadCurrentMonth()
        observePlannedDaysAndOutfits()
    }

    private fun loadCurrentMonth() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        
        _uiState.value = _uiState.value.copy(
            currentYear = year,
            currentMonth = month,
            calendarDays = (1..daysInMonth).toList()
        )
    }

    private fun observePlannedDaysAndOutfits() {
        viewModelScope.launch {
            combine(
                plannedDaysRepository.getAllPlannedDays(),
                outfitRepository.getUserOutfits()
            ) { plannedDays, outfits ->
                Pair(plannedDays, outfits)
            }
            .catch { e ->
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message,
                    isLoading = false
                )
            }
            .collect { (plannedDays, outfits) ->
                _uiState.value = _uiState.value.copy(
                    plannedDays = plannedDays,
                    outfits = outfits,
                    isLoading = false
                )
            }
        }
    }

    fun setViewMode(mode: PlannerViewMode) {
        _uiState.value = _uiState.value.copy(viewMode = mode)
    }

    fun selectDay(day: Int) {
        _uiState.value = _uiState.value.copy(selectedDay = day)
    }

    fun clearSelectedDay() {
        _uiState.value = _uiState.value.copy(selectedDay = null)
    }

    fun getDateStringForDay(day: Int): String {
        val state = _uiState.value
        return DateUtils.format(state.currentYear, state.currentMonth, day)
    }

    fun getPlannedItemsForDay(day: Int): List<PlannedOutfitDisplay> {
        val dateString = getDateStringForDay(day)
        val plannedDay = _uiState.value.plannedDays.find { it.date == dateString }
        val outfitsMap = _uiState.value.outfits.associateBy { it.id }
        
        return plannedDay?.items?.mapNotNull { item ->
            val outfit = outfitsMap[item.outfitId]
            if (outfit != null) {
                PlannedOutfitDisplay(
                    outfitId = item.outfitId,
                    outfitTitle = outfit.name,
                    label = item.label,
                    imageUrl = outfit.thumbnailUrl
                )
            } else null
        } ?: emptyList()
    }

    fun isDayOccupied(day: Int): Boolean {
        val dateString = getDateStringForDay(day)
        return _uiState.value.plannedDays.any { it.date == dateString && it.items.isNotEmpty() }
    }

    fun isDayPast(day: Int): Boolean {
        val state = _uiState.value
        val today = Calendar.getInstance()
        val todayYear = today.get(Calendar.YEAR)
        val todayMonth = today.get(Calendar.MONTH)
        val todayDay = today.get(Calendar.DAY_OF_MONTH)
        
        return when {
            state.currentYear < todayYear -> true
            state.currentYear > todayYear -> false
            state.currentMonth < todayMonth -> true
            state.currentMonth > todayMonth -> false
            else -> day < todayDay
        }
    }

    /**
     * Returns upcoming planned events (today and future) with outfit details.
     * Limited to 4 events for UI display.
     */
    fun getUpcomingEvents(): List<UpcomingEventDisplay> {
        val todayString = DateUtils.today()
        val outfitsMap = _uiState.value.outfits.associateBy { it.id }
        
        return _uiState.value.plannedDays
            .filter { it.date >= todayString && it.items.isNotEmpty() }
            .sortedBy { it.date }
            .take(4)
            .flatMap { plannedDay ->
                plannedDay.items.mapNotNull { item ->
                    val outfit = outfitsMap[item.outfitId]
                    if (outfit != null) {
                        UpcomingEventDisplay(
                            outfitId = item.outfitId,
                            outfitName = outfit.name,
                            label = item.label,
                            date = plannedDay.date,
                            imageUrl = outfit.thumbnailUrl
                        )
                    } else null
                }
            }
            .take(4)
    }

    fun removeOutfitFromDay(day: Int, outfitId: String) {
        viewModelScope.launch {
            try {
                val dateString = getDateStringForDay(day)
                plannedDaysRepository.removeOutfitFromDay(dateString, outfitId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

data class PlannerUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val viewMode: PlannerViewMode = PlannerViewMode.MONTHLY,
    val currentYear: Int = 2026,
    val currentMonth: Int = 0, // 0-indexed (January = 0)
    val calendarDays: List<Int> = (1..30).toList(),
    val plannedDays: List<PlannedDay> = emptyList(),
    val outfits: List<Outfit> = emptyList(),
    val selectedDay: Int? = null
)

data class PlannedOutfitDisplay(
    val outfitId: String,
    val outfitTitle: String,
    val label: String,
    val imageUrl: String? = null
)

data class UpcomingEventDisplay(
    val outfitId: String,
    val outfitName: String,
    val label: String,
    val date: String,
    val imageUrl: String? = null
)

enum class PlannerViewMode {
    MONTHLY, WEEKLY
}
