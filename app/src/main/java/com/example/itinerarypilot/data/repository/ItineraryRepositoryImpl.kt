package com.example.itinerarypilot.data.repository

import com.example.itinerarypilot.data.remote.GeminiDataSource
import com.example.itinerarypilot.domain.model.Itinerary
import com.example.itinerarypilot.domain.model.ItineraryRequest
import com.example.itinerarypilot.domain.repository.ItineraryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ItineraryRepositoryImpl @Inject constructor(
    private val geminiDataSource: GeminiDataSource
) : ItineraryRepository {

    private val _currentItinerary = MutableStateFlow<Itinerary?>(null)
    override val currentItinerary: StateFlow<Itinerary?> = _currentItinerary.asStateFlow()

    override suspend fun generateItinerary(request: ItineraryRequest): Result<Itinerary> =
        runCatching { geminiDataSource.generateItinerary(request) }
            .also { result -> result.getOrNull()?.let { _currentItinerary.value = it } }
}
