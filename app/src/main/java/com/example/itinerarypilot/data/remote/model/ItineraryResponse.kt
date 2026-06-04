package com.example.itinerarypilot.data.remote.model

import com.example.itinerarypilot.domain.model.DayPlan
import com.example.itinerarypilot.domain.model.Itinerary
import com.example.itinerarypilot.domain.model.Place
import kotlinx.serialization.Serializable

@Serializable
data class ItineraryResponse(
    val city: String,
    val totalDays: Int,
    val days: List<DayPlanResponse>,
    val tips: List<String> = emptyList()
)

@Serializable
data class DayPlanResponse(
    val dayNumber: Int,
    val theme: String,
    val places: List<PlaceResponse>
)

@Serializable
data class PlaceResponse(
    val name: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val category: String,
    val estimatedDurationMinutes: Int,
    val bestTimeToVisit: String = ""
)

fun ItineraryResponse.toDomain() = Itinerary(
    city = city,
    totalDays = totalDays,
    days = days.map { it.toDomain() },
    tips = tips
)

fun DayPlanResponse.toDomain() = DayPlan(
    dayNumber = dayNumber,
    theme = theme,
    places = places.map { it.toDomain() }
)

fun PlaceResponse.toDomain() = Place(
    name = name,
    description = description,
    latitude = latitude,
    longitude = longitude,
    category = category,
    estimatedDurationMinutes = estimatedDurationMinutes,
    bestTimeToVisit = bestTimeToVisit
)
