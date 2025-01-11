package ai.koda.mobile.sdk.sample

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GenerateTokenEventParameters(
    @SerialName("next_block_id") val nextBlockId: String
)