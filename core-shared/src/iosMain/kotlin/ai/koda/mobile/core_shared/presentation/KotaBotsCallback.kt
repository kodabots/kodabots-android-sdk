package ai.koda.mobile.core_shared.presentation

interface KodaBotsCallback

data class KodaBotsEvent(
    val type: String,
    val params: Map<String, String>
) : KodaBotsCallback

data class KodaBotsError(
    val error: String
) : KodaBotsCallback