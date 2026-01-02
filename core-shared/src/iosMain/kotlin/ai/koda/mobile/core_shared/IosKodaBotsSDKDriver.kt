package ai.koda.mobile.core_shared

import ai.koda.mobile.core_shared.api.KodaBotsRestApi
import ai.koda.mobile.core_shared.config.KodaBotsConfig
import ai.koda.mobile.core_shared.data.IosKodaBotsPreferencesServices
import ai.koda.mobile.core_shared.data.KodaBotsPreferences
import ai.koda.mobile.core_shared.model.UserProfile
import ai.koda.mobile.core_shared.model.api.CallResponse
import ai.koda.mobile.core_shared.presentation.IosKodaBotsWebViewScreen
import ai.koda.mobile.core_shared.presentation.KodaBotsCallback
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

    private var _isInitialized = false
    override val isInitialized: Boolean
        get() = _isInitialized

    private var _clientToken: String? = null
    override val clientToken: String?
        get() = _clientToken

    private var restApi: KodaBotsRestApi? = null

    /**
     * Method used to initialize SDK.
     * Fetches ClientToken from Info.plist file.
     *
     * @return Boolean value that indicates init state
     */
    override fun init(): Boolean {
        // Fetch client token from Info.plist
        _clientToken = config.customClientId
            ?: (NSBundle.mainBundle.objectForInfoDictionaryKey("KodaBotsSDK") as? Map<String, String>)?.getValue(
                "clientToken"
            )
        if (_clientToken != null) {
            _isInitialized = true
            restApi = KodaBotsRestApi()
            KodaBotsPreferences.initialize(
                IosKodaBotsPreferencesServices.shared
            )

            println("KodaBotsSDK: Initialized successfully with client token")
        } else {
            println("KodaBotsSDK: Failed to get ClientToken, please check your Info.plist")
        }

        return _isInitialized
    }

    /**
     * Method used to uninitialize SDK.
     */
    override fun uninitialize() {
        _isInitialized = false
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
        val userId = KodaBotsPreferences.userId
        val token = clientToken
        val api = restApi

        if (userId != null && token != null && api != null) {
            // Use simple coroutine approach for iOS
            @OptIn(DelicateCoroutinesApi::class)
            GlobalScope.launch {
                val response = try {
                    api.getUnreadCount()
                } catch (e: Exception) {
                    CallResponse.Error(e)
                }

                // Switch back to main thread for callback
                withContext(Dispatchers.Main) {
                    callback.invoke(response)
                }
            }
        } else {
            callback.invoke(CallResponse.Error(Exception("SDK not properly initialized: userId=$userId, token=$token, api=$api")))
        }
    }

    /**
     * Method used to get unread messages count
     * This maintains the suspend function for compatibility
     *
     * @return Sealed class with result
     */
    override suspend fun getUnreadCount(): CallResponse<Int?> {
        val userId = KodaBotsPreferences.userId
        val token = clientToken
        val api = restApi

        return if (userId != null && token != null && api != null) {
            try {
                api.getUnreadCount()
            } catch (e: Exception) {
                CallResponse.Error(e)
            }
        } else {
            CallResponse.Error(Exception("SDK not properly initialized: userId=$userId, token=$token, api=$api"))
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
                println("KodaBotsSDK: Generating iOS WebView Screen with config: $customConfig")
                this@IosKodaBotsSDKDriver.callbacks.let { driverCallbacks ->
                    driverCallbacks?.let {
                        this.callbacks = it
                    }
                }
            }
        } else {
            println("KodaBotsSDK: Cannot generate view controller - SDK not initialized")
            null
        }
    }
}
