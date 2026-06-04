package com.example.itinerarypilot.domain.model

data class ItineraryRequest(
    val city: String,
    val days: Int,
    val interests: List<Interest> = emptyList()
)
