package ai.koda.mobile.core_shared.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

@OptIn(ExperimentalSerializationApi::class)
@Serializable
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