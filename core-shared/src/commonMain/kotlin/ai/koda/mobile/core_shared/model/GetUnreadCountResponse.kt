package ai.koda.mobile.core_shared.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

@OptIn(ExperimentalSerializationApi::class)
@Serializable
class GetUnreadCountResponse {
    var status: String? = null
    var message: String? = null
    var response: GetUnreadCountResponseData? = null
}