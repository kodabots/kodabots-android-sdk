package ai.koda.mobile.sdk.core

import androidx.annotation.Keep
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable

@Serializable
@Keep
@OptIn(InternalSerializationApi::class)
data class UserProfile(
    val first_name: String? = null,
    val last_name: String? = null,
    val email: String? = null,
    val os: String? = null,
    val os_version: String? = null,
    val webview_user_agent: String? = null,
    val locale: String? = null,
    val model: String? = null,
    val manufacturer: String? = null,
    /**
     * Parameter used to pass custom data to KodaBots chatbot
     */
    val custom_parameters: HashMap<String, String> = HashMap()
)

@Serializable
@Keep
@OptIn(InternalSerializationApi::class)
data class GetUnreadCountResponse(
    val status: String? = null,
    val message: String? = null,
    val response: GetUnreadCountResponseData? = null
)

@Serializable
@Keep
@OptIn(InternalSerializationApi::class)
data class GetUnreadCountResponseData(
    val unread_counter: Int? = null
)