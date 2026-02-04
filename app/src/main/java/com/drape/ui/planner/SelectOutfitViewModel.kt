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
import javax.inject.Inject

@HiltViewModel
class SelectOutfitViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val outfitRepository: OutfitRepository,
    private val plannedDaysRepository: PlannedDaysRepository
) : ViewModel() {

    // Navigation arguments
    private val day: Int = savedStateHandle["day"] ?: 1
    private val month: Int = savedStateHandle["month"] ?: 0
    private val year: Int = savedStateHandle["year"] ?: 2026

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
