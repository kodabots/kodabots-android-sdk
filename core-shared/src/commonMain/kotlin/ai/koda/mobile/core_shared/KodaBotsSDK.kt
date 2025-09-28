package ai.koda.mobile.core_shared

import ai.koda.mobile.core_shared.screen.KodaBotsWebViewScreen

abstract class KodaBotsSDK {

    abstract var isInitialized: Boolean
    protected set

    abstract var clientToken: String?

    abstract fun init(): Boolean

    abstract fun uninitialize()

    abstract fun gatherPhoneData()
}

expect fun generateScreen(): KodaBotsWebViewScreen