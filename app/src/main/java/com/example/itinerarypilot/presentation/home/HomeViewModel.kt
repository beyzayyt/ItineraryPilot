package com.example.itinerarypilot.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.itinerarypilot.domain.model.Interest
import com.example.itinerarypilot.domain.model.ItineraryRequest
import com.example.itinerarypilot.domain.repository.ItineraryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val city: String = "",
    val days: Int = 3,
    val selectedInterests: Set<Interest> = emptySet(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val navigateToItinerary: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: ItineraryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun onCityChange(city: String) {
        _uiState.update { it.copy(city = city, error = null) }
    }

    fun onDaysChange(days: Int) {
        _uiState.update { it.copy(days = days) }
    }

    fun onInterestToggle(interest: Interest) {
        _uiState.update { state ->
            val updated = if (interest in state.selectedInterests) {
                state.selectedInterests - interest
            } else {
                state.selectedInterests + interest
            }
            state.copy(selectedInterests = updated)
        }
    }

    fun onGenerateItinerary() {
        val state = _uiState.value
        if (state.city.isBlank()) {
            _uiState.update { it.copy(error = "Please enter a city name") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val request = ItineraryRequest(
                city = state.city.trim(),
                days = state.days,
                interests = state.selectedInterests.toList()
            )
            repository.generateItinerary(request)
                .onSuccess {
                    _uiState.update {
                        it.copy(isLoading = false, navigateToItinerary = true)
                    }
                }
                .onFailure { error ->
                    val errorMessage = when {
                        error.message?.contains("RESOURCE_EXHAUSTED", ignoreCase = true) == true ||
                        error.message?.contains("Quota exceeded", ignoreCase = true) == true ||
                        error.message?.contains("free_tier", ignoreCase = true) == true ||
                        error.message?.contains("429", ignoreCase = true) == true -> {
                            "Servis şu an yoğun. Lütfen birkaç dakika bekleyip tekrar deneyin."
                        }
                        error.message?.contains("401", ignoreCase = true) == true ||
                        error.message?.contains("403", ignoreCase = true) == true -> {
                            "Servis erişimi reddedildi. Lütfen daha sonra tekrar deneyin."
                        }
                        error.message?.contains("UnknownHostException", ignoreCase = true) == true ||
                        error.message?.contains("SocketTimeoutException", ignoreCase = true) == true ||
                        error.message?.contains("timeout", ignoreCase = true) == true -> {
                            "İnternet bağlantınızı kontrol edip tekrar deneyin."
                        }
                        else -> "Bir şeyler ters gitti. Lütfen tekrar deneyin."
                    }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = errorMessage
                        )
                    }
                }
        }
    }

    fun onNavigationHandled() {
        _uiState.update { it.copy(navigateToItinerary = false) }
    }
}
