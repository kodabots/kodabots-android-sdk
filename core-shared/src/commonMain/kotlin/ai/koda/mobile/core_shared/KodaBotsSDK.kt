package ai.koda.mobile.core_shared

import ai.koda.mobile.core_shared.model.UserProfile
import ai.koda.mobile.core_shared.model.api.CallResponse
import ai.koda.mobile.core_shared.screen.KodaBotsWebViewScreen

object KodaBotsSDK {

    lateinit var driver: KodaBotsSDKDriver

    var clientToken: String? = null
        get() = driver.clientToken

    fun init(
        driver: KodaBotsSDKDriver
    ): Boolean {
        this.driver = driver
        return this.driver.init()
    }

    fun gatherPhoneData(userProfile: UserProfile? = null): UserProfile? {
        return driver.gatherPhoneData(userProfile)
    }

    fun getUnreadCount(callback: (CallResponse<Int?>) -> Unit) {
        return driver.getUnreadCount(callback)
    }

    suspend fun getUnreadCount(): CallResponse<Int?> {
        return driver.getUnreadCount()
    }

    fun generateScreen(): Any? {
        return driver.generateScreen()
    }

    fun uninitialize() {
        driver.uninitialize()
    }
}

interface KodaBotsSDKDriver {
    var isInitialized: Boolean

    var clientToken: String?

    fun init(): Boolean

    fun uninitialize()

    fun gatherPhoneData(userProfile: UserProfile? = null): UserProfile?

    fun getUnreadCount(callback: (CallResponse<Int?>) -> Unit)

    suspend fun getUnreadCount(): CallResponse<Int?>

    fun generateScreen(): Any?
}