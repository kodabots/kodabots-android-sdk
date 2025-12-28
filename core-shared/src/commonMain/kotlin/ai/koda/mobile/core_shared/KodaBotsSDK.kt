package ai.koda.mobile.core_shared

import ai.koda.mobile.core_shared.model.UserProfile
import ai.koda.mobile.core_shared.model.api.CallResponse
import ai.koda.mobile.core_shared.screen.KodaBotsWebViewScreen

object KodaBotsSDK {

    var driver: KodaBotsSDKDriver? = null
        private set

    val isInitialized: Boolean
        get() = driver?.isInitialized == true

    var clientToken: String? = null
        get() = driver?.clientToken

    fun init(
        driver: KodaBotsSDKDriver
    ): Boolean {
        this.driver = driver
        return this.driver?.init() ?: false
    }

    fun gatherPhoneData(userProfile: UserProfile? = null): UserProfile? {
        return driver?.gatherPhoneData(userProfile)
    }

    fun getUnreadCount(callback: (CallResponse<Int?>) -> Unit) {
        driver?.getUnreadCount(callback)
    }

    suspend fun getUnreadCount(): CallResponse<Int?> {
        return driver?.getUnreadCount() ?: CallResponse.Error(Exception("SDK not initialized"))
    }

    fun generateScreen(): Any? {
        return driver?.generateScreen()
    }

    fun uninitialize() {
        driver?.uninitialize()
        driver = null
    }
}

interface KodaBotsSDKDriver {
    val isInitialized: Boolean

    val clientToken: String?

    fun init(): Boolean

    fun uninitialize()

    fun gatherPhoneData(userProfile: UserProfile? = null): UserProfile?

    fun getUnreadCount(callback: (CallResponse<Int?>) -> Unit)

    suspend fun getUnreadCount(): CallResponse<Int?>

    fun generateScreen(): Any?
}