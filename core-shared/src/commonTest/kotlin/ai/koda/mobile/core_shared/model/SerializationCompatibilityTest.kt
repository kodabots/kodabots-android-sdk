package ai.koda.mobile.core_shared.model

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class SerializationCompatibilityTest {
    @Test
    fun userProfileRetainsCustomParametersAfterJsonRoundTrip() {
        val profile = UserProfile().apply {
            first_name = "Test"
            custom_parameters["language"] = "pl"
        }

        val decoded = Json.decodeFromString<UserProfile>(Json.encodeToString(profile))

        assertEquals("Test", decoded.first_name)
        assertEquals(mapOf("language" to "pl"), decoded.custom_parameters)
    }

    @Test
    fun unreadCountResponseDecodesNestedDataWithUnknownFields() {
        val json = Json { ignoreUnknownKeys = true }
        val decoded = json.decodeFromString<GetUnreadCountResponse>(
            """{"status":"ok","response":{"unread_counter":3,"extra":true},"extra":"ignored"}"""
        )

        assertEquals("ok", decoded.status)
        assertEquals(3, assertNotNull(decoded.response).unread_counter)
    }
}
