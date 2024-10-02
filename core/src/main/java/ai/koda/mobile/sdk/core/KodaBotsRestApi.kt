package ai.koda.mobile.sdk.core

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.url
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

internal class KodaBotsRestApi {
    private val client = HttpClient {
        expectSuccess = true

        install(ContentNegotiation) {
            json(
                Json {
                    encodeDefaults = true
                    ignoreUnknownKeys = true
                    isLenient = true
                    allowStructuredMapKeys = true
                    prettyPrint = true
                    useArrayPolymorphism = false
                    classDiscriminator = "type"
                }
            )
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

    internal suspend fun getUnreadCount() =
        try {
            val unreadCountResponse: GetUnreadCountResponse = client.get {
                url(endpoint(UNREAD_MESSAGES))
                headers {
                    append(Header.BOT_TOKEN, KodaBotsSDK.clientToken ?: "")
                    append(Header.USER_ID, KodaBotsPreferences.userId ?: "")
                }
            }.body()

            CallResponse.Success(unreadCountResponse.response?.unread_counter)
        } catch (e: ClientRequestException) {
            val exception = when (e.response.status.value) {
                CODE_UNAUTHORIZED -> Exception("Unauthorized")
                CODE_FORBIDDEN -> Exception("Forbidden")
                else -> Exception(e.message)
            }

            CallResponse.Error(exception)
        } catch (t: Throwable) {

            CallResponse.Error(Exception(t.message))
        }

    companion object {
        private const val CODE_UNAUTHORIZED = 401
        private const val CODE_FORBIDDEN = 403
        private const val BASE_URL =
            "${BuildConfig.REST_BASE_URL}/sdk/${BuildConfig.REST_API_VERSION}"

        /** Endpoints paths */
        const val UNREAD_MESSAGES = "/unread-counter"
    }

    private object Header {
        const val USER_ID = "kodabots-bot-user-id"
        const val BOT_TOKEN = "kodabots-bot-token"
    }

    private fun endpoint(relativePath: String) = BASE_URL + relativePath
}