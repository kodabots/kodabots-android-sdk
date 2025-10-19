package ai.koda.mobile.core_shared

import ai.koda.mobile.core_shared.api.KodaBotsRestApi
import ai.koda.mobile.core_shared.config.KodaBotsConfig
import ai.koda.mobile.core_shared.data.IosKodaBotsPreferencesServices
import ai.koda.mobile.core_shared.data.KodaBotsPreferences
import ai.koda.mobile.core_shared.model.UserProfile
import ai.koda.mobile.core_shared.model.api.CallResponse
import ai.koda.mobile.core_shared.presentation.IosKodaBotsWebViewScreen
import ai.koda.mobile.core_shared.presentation.KodaBotsCallback
import ai.koda.mobile.core_shared.screen.KodaBotsWebViewScreen
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import platform.Foundation.NSBundle
import platform.UIKit.UIDevice
import platform.WebKit.WKWebView

@OptIn(ExperimentalForeignApi::class)
class IosKodaBotsSDKDriver(
    private val config: KodaBotsConfig = KodaBotsConfig(),
    private val callbacks: ((KodaBotsCallback) -> Unit)? = null
) : KodaBotsSDKDriver {

    private val KODA_CLIENT_TOKEN_KEY = "KodaBotsClientToken"

    override var isInitialized = false
    override var clientToken: String? = null

    private var restApi: KodaBotsRestApi? = null

    /**
     * Method used to initialize SDK.
     * Fetches ClientToken from Info.plist file.
     *
     * @return Boolean value that indicates init state
     */
    override fun init(): Boolean {
        // Fetch client token from Info.plist
        // TODO: Uncomment it
//        clientToken =
//            NSBundle.mainBundle.objectForInfoDictionaryKey(KODA_CLIENT_TOKEN_KEY) as? String
        clientToken = "test"
        if (clientToken != null) {
            isInitialized = true
            restApi = KodaBotsRestApi()
            KodaBotsPreferences.initialize(
                IosKodaBotsPreferencesServices.shared
            )

            println("KodaBotsSDK: Initialized successfully with client token")
        } else {
            println("KodaBotsSDK: Failed to get ClientToken, please check your Info.plist")
        }

        return isInitialized
    }

    /**
     * Method used to uninitialize SDK.
     */
    override fun uninitialize() {
        isInitialized = false
        println("KodaBotsSDK: SDK uninitialized")
    }

    override fun gatherPhoneData(userProfile: UserProfile?): UserProfile? {
        val webview = WKWebView()
        // Get user agent using proper iOS API
        val userAgentString = webview.customUserAgent
            ?: "Mozilla/5.0 (iPhone; CPU iPhone OS like Mac OS X) AppleWebKit/605.1.15"
        val systemVersion = UIDevice.currentDevice.systemVersion
        val modelName = UIDevice.currentDevice.model
        val language = (NSBundle.mainBundle.preferredLocalizations.firstOrNull() as? String) ?: "en"

        println(
            "KodaBotsSDK: UserAgent: $userAgentString ; System Version: $systemVersion ; Model Name: $modelName ; Language: $language"
        )

        userProfile?.apply {
            this.manufacturer = "Apple"
            this.model = modelName
            this.os = "iOS"
            this.os_version = systemVersion
            this.webview_user_agent = userAgentString
            this.locale = language
        }
        return userProfile
    }

    /**
     * Method used to get unread messages count using iOS native patterns
     *
     * @param callback Callback that returns sealed class with result
     */
    override fun getUnreadCount(callback: (CallResponse<Int?>) -> Unit) {
        if (KodaBotsPreferences.userId != null && clientToken != null) {
            // Use simple coroutine approach for iOS
            @OptIn(DelicateCoroutinesApi::class)
            GlobalScope.launch {
                val response = try {
                    restApi?.getUnreadCount()
                        ?: CallResponse.Error(Exception("Rest API not initialized properly"))
                } catch (e: Exception) {
                    CallResponse.Error(e)
                }

                // Switch back to main thread for callback
                withContext(Dispatchers.Main) {
                    callback.invoke(response)
                }
            }
        } else {
            callback.invoke(CallResponse.Error(Exception("UserID or ClientToken are null")))
        }
    }

    /**
     * Method used to get unread messages count
     * This maintains the suspend function for compatibility
     *
     * @return Sealed class with result
     */
    override suspend fun getUnreadCount(): CallResponse<Int?> {
        return if (KodaBotsPreferences.userId != null && clientToken != null) {
            try {
                restApi?.getUnreadCount()
                    ?: CallResponse.Error(Exception("Rest API not initialized properly"))
            } catch (e: Exception) {
                CallResponse.Error(e)
            }
        } else {
            CallResponse.Error(Exception("UserID or ClientToken are null"))
        }
    }

    override fun generateScreen(): Any? {
        return generateViewController()
    }

    /**
     * If SDK is initialized, will return IosKodaBotsWebViewScreen that you can display and use.
     * This method follows iOS patterns more closely.
     *
     * @return IosKodaBotsWebViewScreen (iOS UIViewController implementation)
     */
    private fun generateViewController(): Any? {
        return if (isInitialized) {
            IosKodaBotsWebViewScreen().apply {
                customConfig = config
                callbacks.let {
                    this.callbacks = it
                }
            }
        } else {
            println("KodaBotsSDK: Cannot generate view controller - SDK not initialized")
            null
        }
    }
}
