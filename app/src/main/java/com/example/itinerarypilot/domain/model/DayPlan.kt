package com.example.itinerarypilot.domain.model

data class DayPlan(
    val dayNumber: Int,
    val theme: String,
    val places: List<Place>
)
