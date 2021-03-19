package com.kodabots.sdk.core

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Serializable
@Keep
class UserProfile {
    var first_name: String? = null
    var last_name: String? = null
    var email: String? = null
    var os: String? = null
    var os_version: String? = null
    var webview_user_agent: String? = null
    var locale: String? = null
    var model: String? = null
    var manufacturer: String? = null

    /**
     * Parameter used to pass custom data to KodaBots chatbot
     */
    var custom_parameters:HashMap<String,String> = HashMap()
}

@Serializable
@Keep
class GetUnreadCountResponse {
    var status: String? = null
    var message: String? = null
    var response: GetUnreadCountResponseData? = null
}

@Serializable
@Keep
class GetUnreadCountResponseData {
    var unread_counter: Int? = null
}