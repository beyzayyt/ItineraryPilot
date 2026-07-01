package com.beyzayyt.itinerarypilot.domain.repository

import com.beyzayyt.itinerarypilot.domain.model.Itinerary
import com.beyzayyt.itinerarypilot.domain.model.ItineraryRequest
import kotlinx.coroutines.flow.StateFlow

interface ItineraryRepository {
    val currentItinerary: StateFlow<Itinerary?>
    suspend fun generateItinerary(request: ItineraryRequest): Result<Itinerary>
}
