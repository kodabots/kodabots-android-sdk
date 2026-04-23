package ai.koda.mobile.core_shared

import ai.koda.mobile.core_shared.api.KodaBotsRestApi
import ai.koda.mobile.core_shared.config.KodaBotsConfig
import ai.koda.mobile.core_shared.data.AndroidKodaBotsPreferencesServices
import ai.koda.mobile.core_shared.data.KodaBotsPreferences
import ai.koda.mobile.core_shared.model.UserProfile
import ai.koda.mobile.core_shared.model.api.CallResponse
import ai.koda.mobile.core_shared.presentation.KodaBotsCallbacks
import ai.koda.mobile.core_shared.presentation.KodaBotsWebViewFragment
import ai.koda.mobile.core_shared.screen.KodaBotsWebViewScreen
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.webkit.WebView
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.Locale

class AndroidKodaBotsSDKDriver(
    private val context: Context,
    private val config: KodaBotsConfig = KodaBotsConfig(),
    private val callbacks: ((KodaBotsCallbacks) -> Unit)? = null
) : KodaBotsSDKDriver {

    private val KODA_CLIENT_TOKEN_KEY = "ai.koda.mobile.sdk.ClientToken"

    internal val globalExceptionHandler = CoroutineExceptionHandler { _, exception ->
        Log.e("KodaBotsSDK", "Coroutine exception: ${exception.message}")
    }

    private var kodaClientScope: CoroutineScope? = null

    private var _isInitialized = false
    override val isInitialized: Boolean
        get() = _isInitialized

    private var _clientToken: String? = null
    override val clientToken: String?
        get() = _clientToken

    private var restApi: KodaBotsRestApi? = null

    /**
     * Method used to initialize SDK.
     * Fetches ClientToken from Manifest file.
     *
     * @param context
     * @return Boolean value that indicates init state
     */
    override fun init(): Boolean {
        kodaClientScope?.cancel()
        kodaClientScope = CoroutineScope(SupervisorJob() + globalExceptionHandler)

        _clientToken = config.customClientId
            ?: context.packageManager
                .getApplicationInfo(
                    context.packageName,
                    PackageManager.GET_META_DATA
                ).metaData?.getString(KODA_CLIENT_TOKEN_KEY)

        if (_clientToken != null) {
            _isInitialized = true
            restApi = KodaBotsRestApi(customBaseRestUrl = config.customBaseRestUrl)
            KodaBotsPreferences.initialize(
                AndroidKodaBotsPreferencesServices(context)
            )
        } else {
            Log.d("KodaBotsSDK", "Failed to get ClientId, please check your AndroidManifest")
        }

        return _isInitialized
    }


    /**
     * Method used to uninitialize SDK.
     */
    override fun uninitialize() {
        kodaClientScope?.cancel("Koda SDK uninitialized")
        _isInitialized = false
    }

    override fun gatherPhoneData(userProfile: UserProfile?): UserProfile? {
        Log.d(
            "KodaBotsSDK",
            "Manufacturer: ${Build.MANUFACTURER} ; Model: ${Build.MODEL} ; SDK: ${Build.VERSION.SDK_INT} ; User Agent String: ${
                WebView(context).settings.userAgentString
            } ; Language: ${Locale.getDefault().displayCountry}"
        )
        userProfile?.apply {
            this.manufacturer = Build.MANUFACTURER
            this.model = Build.MODEL
            this.os = "Android"
            this.os_version = Build.VERSION.SDK_INT.toString()
            this.webview_user_agent = WebView(context).settings.userAgentString
            this.locale = Locale.getDefault().displayCountry
        }
        return userProfile
    }

    /**
     * Method used to get unread messages count
     *
     * @param callback Callback that returns sealed class with result
     */
    override fun getUnreadCount(callback: (CallResponse<Int?>) -> Unit) {
        val userId = KodaBotsPreferences.userId
        val token = clientToken
        val api = restApi
        val scope = kodaClientScope

        if (userId != null && token != null && api != null && scope != null) {
            scope.launch(globalExceptionHandler) {
                val response = api.getUnreadCount()
                callback.invoke(response)
            }
        } else {
            callback.invoke(CallResponse.Error(Exception("SDK not properly initialized: userId=$userId, token=$token, api=$api, scope=$scope")))
        }
    }


    /**
     * Method used to get unread messages count
     *
     * @return Sealed class with result
     */
    override suspend fun getUnreadCount(): CallResponse<Int?> {
        val userId = KodaBotsPreferences.userId
        val token = clientToken
        val api = restApi

        return if (userId != null && token != null && api != null) {
            api.getUnreadCount()
        } else {
            CallResponse.Error(Exception("SDK not properly initialized: userId=$userId, token=$token, api=$api"))
        }
    }

    override fun generateScreen(): KodaBotsWebViewScreen? {
        return generateFragment(
            config,
            callbacks
        )
    }

    /**
     * If SDK is initialized, will return KodaBotsWebViewFragment that you can display and use.
     *
     * @param config Configuration file for SDK. You can set userProfile, conversation blockId and progress/timeout ui customisation
     * @param callbacks Callbacks from KodaBots chatbot
     * @return KodaBotsWebViewFragment
     */
    fun generateFragment(
        config: KodaBotsConfig? = null,
        callbacks: ((KodaBotsCallbacks) -> Unit)? = null
    ): KodaBotsWebViewFragment? {
        return if (isInitialized) KodaBotsWebViewFragment().apply {
            config?.let {
                customConfig = it
            }
            callbacks?.let {
                this.callbacks = it
            }
        } else null
    }
}
