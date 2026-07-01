package com.beyzayyt.itinerarypilot.domain.model

data class Itinerary(
    val city: String,
    val totalDays: Int,
    val days: List<DayPlan>,
    val tips: List<String> = emptyList()
)
