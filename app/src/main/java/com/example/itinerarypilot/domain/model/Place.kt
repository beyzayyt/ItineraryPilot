package com.example.itinerarypilot.domain.model

data class Place(
    val name: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val category: String,
    val estimatedDurationMinutes: Int,
    val bestTimeToVisit: String = ""
)
