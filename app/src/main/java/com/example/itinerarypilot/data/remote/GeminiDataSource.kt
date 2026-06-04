package com.example.itinerarypilot.data.remote

import com.example.itinerarypilot.BuildConfig
import com.example.itinerarypilot.data.remote.model.ItineraryResponse
import com.example.itinerarypilot.data.remote.model.toDomain
import com.example.itinerarypilot.domain.model.Itinerary
import com.example.itinerarypilot.domain.model.ItineraryRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

private const val MAX_RETRIES = 3
private const val INITIAL_DELAY_MS = 1_000L

@Singleton
class GeminiDataSource @Inject constructor() {

    private val client = OkHttpClient.Builder()
        .callTimeout(60, TimeUnit.SECONDS)
        .build()

    private val json = Json { ignoreUnknownKeys = true }
    private val mediaType = "application/json; charset=utf-8".toMediaType()

    private val endpointUrl =
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-lite:generateContent"

    suspend fun generateItinerary(request: ItineraryRequest): Itinerary = withContext(Dispatchers.IO) {
        val interestText = if (request.interests.isEmpty()) "general sightseeing"
        else request.interests.joinToString(", ") { it.label }

        val prompt = buildPrompt(request.city, request.days, interestText)

        val bodyJson = buildJsonObject {
            putJsonObject("generationConfig") {
                put("responseMimeType", "application/json")
                put("temperature", 0.7)
            }
            putJsonArray("contents") {
                add(buildJsonObject {
                    putJsonArray("parts") {
                        add(buildJsonObject { put("text", prompt) })
                    }
                })
            }
        }.toString()

        val httpRequest = Request.Builder()
            .url("$endpointUrl?key=${BuildConfig.GEMINI_API_KEY}")
            .post(bodyJson.toRequestBody(mediaType))
            .build()

        val rawText = retryWithBackoff {
            client.newCall(httpRequest).execute().use { response ->
                val body = response.body?.string() ?: error("Empty response body")
                if (!response.isSuccessful) {
                    val isRetriable = response.code >= 500
                    if (isRetriable) throw RetriableException("Gemini API error ${response.code}: $body")
                    else error("Gemini API error ${response.code}: $body")
                }
                json.parseToJsonElement(body).jsonObject
                    .getValue("candidates").jsonArray
                    .first().jsonObject
                    .getValue("content").jsonObject
                    .getValue("parts").jsonArray
                    .first().jsonObject
                    .getValue("text").jsonPrimitive.content
            }
        }

        json.decodeFromString<ItineraryResponse>(rawText).toDomain()
    }

    private suspend fun <T> retryWithBackoff(block: suspend () -> T): T {
        var delayMs = INITIAL_DELAY_MS
        repeat(MAX_RETRIES - 1) { attempt ->
            try {
                return block()
            } catch (e: RetriableException) {
                delay(delayMs)
                delayMs *= 2
            } catch (e: IOException) {
                delay(delayMs)
                delayMs *= 2
            }
        }
        return block()
    }

    private fun buildPrompt(city: String, days: Int, interests: String): String = """
        Create a $days-day travel itinerary for $city focusing on: $interests.

        Return ONLY valid JSON matching this exact structure (no markdown, no extra text):
        {
          "city": "$city",
          "totalDays": $days,
          "days": [
            {
              "dayNumber": 1,
              "theme": "Descriptive day theme",
              "places": [
                {
                  "name": "Place Name",
                  "description": "1-2 sentence description",
                  "latitude": 41.9028,
                  "longitude": 12.4964,
                  "category": "landmark",
                  "estimatedDurationMinutes": 90,
                  "bestTimeToVisit": "Morning"
                }
              ]
            }
          ],
          "tips": ["Practical tip 1", "Practical tip 2", "Practical tip 3"]
        }

        Rules:
        - Include 4-5 places per day
        - Use real GPS coordinates for every place
        - category must be one of: landmark, museum, restaurant, park, nightlife, hidden_gem, shopping
        - Order places logically to minimize travel time
    """.trimIndent()
}

private class RetriableException(message: String) : Exception(message)
