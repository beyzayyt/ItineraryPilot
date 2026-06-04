package com.example.itinerarypilot.presentation.itinerary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.itinerarypilot.domain.model.Itinerary
import com.example.itinerarypilot.domain.repository.ItineraryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

sealed interface ItineraryUiState {
    data object Loading : ItineraryUiState
    data class Success(val itinerary: Itinerary) : ItineraryUiState
    data object Empty : ItineraryUiState
}

@HiltViewModel
class ItineraryViewModel @Inject constructor(
    repository: ItineraryRepository
) : ViewModel() {

    val uiState: StateFlow<ItineraryUiState> = repository.currentItinerary
        .map { itinerary ->
            if (itinerary != null) ItineraryUiState.Success(itinerary)
            else ItineraryUiState.Empty
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ItineraryUiState.Empty
        )
}
