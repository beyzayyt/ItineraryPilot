package com.example.itinerarypilot.domain.repository

import com.example.itinerarypilot.domain.model.Itinerary
import com.example.itinerarypilot.domain.model.ItineraryRequest
import kotlinx.coroutines.flow.StateFlow

interface ItineraryRepository {
    val currentItinerary: StateFlow<Itinerary?>
    suspend fun generateItinerary(request: ItineraryRequest): Result<Itinerary>
}
