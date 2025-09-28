package ai.koda.mobile.core_shared.model
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KeepGeneratedSerializer
import kotlinx.serialization.Serializable


@OptIn(ExperimentalSerializationApi::class)
@Serializable
class GetUnreadCountResponseData {
    var unread_counter: Int? = null
}