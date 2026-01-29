package com.drape.ui.planner

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PlannerViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(PlannerUiState())
    val uiState: StateFlow<PlannerUiState> = _uiState.asStateFlow()

    fun setViewMode(mode: PlannerViewMode) {
        _uiState.value = _uiState.value.copy(viewMode = mode)
    }
}

data class PlannerUiState(
    val viewMode: PlannerViewMode = PlannerViewMode.MONTHLY,
    val calendarDays: List<Int> = (1..30).toList()
)

enum class PlannerViewMode {
    MONTHLY, WEEKLY
}
