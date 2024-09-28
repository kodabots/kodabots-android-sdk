package com.kodabots.sdk.core

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.features.HttpTimeout
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logger
import io.ktor.client.features.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.readText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.serialization.json.Json

internal class KodaBotsRestApi {
    private val CODE_UNAUTHORIZED = 401
    private val CODE_FORBIDDEN = 403
    private val jsonMapper = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
        isLenient = true
        allowStructuredMapKeys = true
        prettyPrint = true
        useArrayPolymorphism = false
        classDiscriminator = "type"
    }
    private val client = HttpClient {
        install(JsonFeature) {
            serializer = KotlinxSerializer()
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 15000
        }
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    Log.d("KodaBotsSDK", message)
                }
            }
            level = LogLevel.ALL
        }
    }

    internal fun getUnreadCount() =
        GlobalScope.async(Dispatchers.Default + KodaBotsSDK.globalExceptionHandler) {
            try {
                val result: HttpResponse = client.get {
                    url("${BuildConfig.REST_BASE_URL}/sdk/${BuildConfig.REST_API_VERSION}/unread-counter")
                    headers {
                        this.append("kodabots-bot-token", KodaBotsSDK.clientToken ?: "")
                        this.append("kodabots-bot-user-id", KodaBotsPreferences.userId ?: "")
                    }
                }
                when {
                    checkIfUnauthorized(result) -> {
                        CallResponse.Error(Exception("Unauthorized"))
                    }

                    checkIfForbidden(result) -> {
                        CallResponse.Error(Exception("Forbidden"))
                    }

                    else -> {
                        try {
                            val resp = jsonMapper.decodeFromString(
                                GetUnreadCountResponse.serializer(),
                                result.readText(null)
                            )
                            CallResponse.Success(resp.response?.unread_counter)
                        } catch (e: Exception) {
                            CallResponse.Error(Exception("Unable to parse response"))
                        }
                    }
                }
            } catch (t: Throwable) {
                CallResponse.Error(Exception(t.message))
            }
        }


    private fun checkIfUnauthorized(response: HttpResponse): Boolean =
        response.status.value == CODE_UNAUTHORIZED

    private fun checkIfForbidden(response: HttpResponse): Boolean =
        response.status.value == CODE_FORBIDDEN
}