package com.beyzayyt.itinerarypilot.data.remote

import com.beyzayyt.itinerarypilot.BuildConfig
import com.beyzayyt.itinerarypilot.data.remote.model.ItineraryResponse
import com.beyzayyt.itinerarypilot.data.remote.model.toDomain
import com.beyzayyt.itinerarypilot.domain.model.Interest
import com.beyzayyt.itinerarypilot.domain.model.Itinerary
import com.beyzayyt.itinerarypilot.domain.model.ItineraryRequest
import com.google.firebase.appcheck.FirebaseAppCheck
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.JsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

private const val MAX_RETRIES = 3
private const val INITIAL_DELAY_MS = 1_000L

@Singleton
class GeminiDataSource @Inject constructor() {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .callTimeout(120, TimeUnit.SECONDS)
        .build()

    private val json = Json { ignoreUnknownKeys = true }
    private val mediaType = "application/json; charset=utf-8".toMediaType()

    private val proxyUrl =
        "https://generateitinerary-yk7n5kh6oq-uc.a.run.app"

    suspend fun generateItinerary(request: ItineraryRequest): Itinerary = withContext(Dispatchers.IO) {
        val interestText = if (request.interests.isEmpty()) "general sightseeing"
        else request.interests.joinToString(", ") { it.label }

        val bodyJson = buildJsonObject {
            put("city", request.city)
            put("days", request.days)
            putJsonArray("interests") {
                interestText.split(", ").forEach { add(JsonPrimitive(it)) }
            }
        }.toString()

        val appCheckToken = withTimeoutOrNull(5_000L) {
            runCatching { getAppCheckToken() }.getOrElse { "" }
        } ?: ""

        val httpRequest = Request.Builder()
            .url(proxyUrl)
            .apply {
                if (BuildConfig.DEBUG) addHeader("X-Debug-Build", "true")
                if (appCheckToken.isNotEmpty()) addHeader("X-Firebase-AppCheck", appCheckToken)
            }
            .post(bodyJson.toRequestBody(mediaType))
            .build()

        val rawJson = retryWithBackoff {
            client.newCall(httpRequest).execute().use { response ->
                val body = response.body?.string() ?: error("Empty response body")
                if (!response.isSuccessful) {
                    if (response.code >= 500) throw RetriableException("Server error ${response.code}")
                    else error("Request failed ${response.code}: $body")
                }
                body
            }
        }

        json.decodeFromString<ItineraryResponse>(rawJson).toDomain()
    }

    private suspend fun getAppCheckToken(): String =
        suspendCancellableCoroutine { cont ->
            FirebaseAppCheck.getInstance().getAppCheckToken(false)
                .addOnSuccessListener { result -> cont.resume(result.token) }
                .addOnFailureListener { e -> cont.resumeWithException(e) }
        }

    private suspend fun <T> retryWithBackoff(block: suspend () -> T): T {
        var delayMs = INITIAL_DELAY_MS
        repeat(MAX_RETRIES - 1) {
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
}

private class RetriableException(message: String) : Exception(message)
