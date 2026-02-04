package com.drape.ui.planner

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drape.data.model.Outfit
import com.drape.data.repository.OutfitRepository
import com.drape.data.repository.PlannedDaysRepository
import com.drape.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

/**
 * ViewModel for the outfit selection screen.
 * Requires day, month, and year navigation arguments to be present in SavedStateHandle.
 * 
 * @throws IllegalArgumentException if required navigation arguments are missing
 */
@HiltViewModel
class SelectOutfitViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val outfitRepository: OutfitRepository,
    private val plannedDaysRepository: PlannedDaysRepository
) : ViewModel() {

    // Navigation arguments - fail fast if missing
    private val day: Int = requireNotNull(savedStateHandle["day"]) {
        "SelectOutfitViewModel requires 'day' navigation argument"
    }
    private val month: Int = requireNotNull(savedStateHandle["month"]) {
        "SelectOutfitViewModel requires 'month' navigation argument"
    }
    private val year: Int = requireNotNull(savedStateHandle["year"]) {
        "SelectOutfitViewModel requires 'year' navigation argument"
    }

    private val _uiState = MutableStateFlow(SelectOutfitUiState(selectedDay = day))
    val uiState: StateFlow<SelectOutfitUiState> = _uiState.asStateFlow()

    init {
        loadOutfits()
    }

    private fun loadOutfits() {
        viewModelScope.launch {
            outfitRepository.getUserOutfits()
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = e.message
                    )
                }
                .collect { outfits ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        outfits = outfits
                    )
                }
        }
    }

    fun getDateString(): String {
        return DateUtils.format(year, month, day)
    }

    fun getFormattedDate(): String {
        val months = listOf(
            "Gennaio", "Febbraio", "Marzo", "Aprile", "Maggio", "Giugno",
            "Luglio", "Agosto", "Settembre", "Ottobre", "Novembre", "Dicembre"
        )
        return "$day ${months.getOrElse(month) { "" }} $year"
    }

    fun addOutfitToDay(outfitId: String, label: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            try {
                val dateString = getDateString()
                plannedDaysRepository.addOutfitToDay(dateString, outfitId, label)
                _uiState.value = _uiState.value.copy(isSaving = false)
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = e.message
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

data class SelectOutfitUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val selectedDay: Int = 1,
    val outfits: List<Outfit> = emptyList()
)
