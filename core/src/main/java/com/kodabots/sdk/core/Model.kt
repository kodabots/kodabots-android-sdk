package com.kodabots.sdk.core

import kotlinx.serialization.Serializable

@Serializable
class UserProfile {
    var first_name: String? = null
    var last_name: String? = null
    var email: String? = null
    var custom_key: String? = null
    var os: String? = null
    var os_version: String? = null
    var webview_user_agent: String? = null
    var locale: String? = null
    var model: String? = null
    var manufacturer: String? = null
    var custom_parameters:HashMap<String,String> = HashMap()
}

@Serializable
class GetUnreadCountResponse {
    var status: String? = null
    var message: String? = null
    var response: GetUnreadCountResponseData? = null
}

@Serializable
class GetUnreadCountResponseData {
    var unread_counter: Int? = null
}