package com.kodabots.sdk.core

import android.util.Log
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.features.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
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
    private val client = HttpClient() {
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
                        CallResponse.Error<Int?>(Exception("Unauthorized"))
                    }
                    checkIfForbidden(result) -> {
                        CallResponse.Error<Int?>(Exception("Forbidden"))
                    }
                    else -> {
                        try {
                            val resp = jsonMapper.decodeFromString(
                                GetUnreadCountResponse.serializer(),
                                result.readText(null)
                            )
                            CallResponse.Success<Int?>(resp.response?.unread_counter)
                        } catch (e: Exception) {
                            CallResponse.Error<Int?>(Exception("Unable to parse response"))
                        }
                    }
                }
            } catch (t: Throwable) {
                CallResponse.Error<Int?>(Exception(t.message))
            }

        }


    private fun checkIfUnauthorized(response: HttpResponse): Boolean =
        response.status.value == CODE_UNAUTHORIZED

    private fun checkIfForbidden(response: HttpResponse): Boolean =
        response.status.value == CODE_FORBIDDEN
}