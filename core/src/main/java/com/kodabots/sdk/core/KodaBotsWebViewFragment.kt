package com.kodabots.sdk.core

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.RenderMode
import com.airbnb.lottie.model.KeyPath
import com.kodabots.sdk.core.databinding.FragmentKodaBotsWebviewBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeUnit

class KodaBotsWebViewFragment : Fragment(R.layout.fragment_koda_bots_webview) {
    private var binding: FragmentKodaBotsWebviewBinding? = null
    private var scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val chromeClient = KodaBotsChromeClient(this@KodaBotsWebViewFragment)
    private var isReady = false
    private var timeoutDeferred: Deferred<Unit>? = null

    var customConfig: KodaBotsConfig? = KodaBotsConfig()
    var callbacks: (KodaBotsCallbacks) -> Unit = {}

    private val kodaBotUrl
        get() = "${BuildConfig.BASE_URL}/mobile/${BuildConfig.API_VERSION}" +
                "/?token=${KodaBotsSDK.clientToken}"

    private val webviewCallbacks = object : WebviewCallbacks {
        override fun onLoadingFinished() {
            initialize()
        }

        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            return if (request?.url.toString().startsWith("tel", true)) {
                startActivity(Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse(request?.url.toString())
                })
                true
            } else {
                false
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentKodaBotsWebviewBinding.bind(view)
        setupProgress()
        setupWentWrong()
        binding?.fragmentKodaBotsWebview?.apply {
            settings.apply {
                this.javaScriptEnabled = true
                this.userAgentString = "KodaBotsSDK " + this.userAgentString
                this.allowFileAccessFromFileURLs = true
                this.allowContentAccess = true
                this.allowUniversalAccessFromFileURLs = true
                this.allowFileAccess = true
                this.loadWithOverviewMode = true
                this.useWideViewPort = true
                this.domStorageEnabled = true
                this.setSupportMultipleWindows(true)
                this.javaScriptCanOpenWindowsAutomatically = true
                this.mediaPlaybackRequiresUserGesture = false
                this.defaultTextEncodingName = "UTF-8"
            }
            if (BuildConfig.DEBUG) {
                WebView.setWebContentsDebuggingEnabled(true)
            }
            CookieManager.getInstance().apply {
                setAcceptCookie(true)
                setAcceptThirdPartyCookies(binding?.fragmentKodaBotsWebview, true)
            }
            addJavascriptInterface(this@KodaBotsWebViewFragment, "android")
            webChromeClient = chromeClient
            webViewClient = KodaBotsWebViewClient(webviewCallbacks)
            clearCache(true)
        }.also {
            loadUrl()
        }
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    private fun loadUrl() {
        setLoadingViewVisibility(isVisible = true)
        setErrorViewVisibility(isVisible = false)
        binding?.fragmentKodaBotsWebview?.loadUrl(kodaBotUrl)
        timeoutDeferred = scope.async(Dispatchers.Main + KodaBotsSDK.globalExceptionHandler) {
            delay(
                TimeUnit.SECONDS.toMillis(
                    customConfig?.timeoutConfig?.timeout ?: DEFAULT_WENT_WRONG_TIMEOUT
                )
            )
            setErrorViewVisibility(isVisible = true)
        }
    }

    private fun setErrorViewVisibility(isVisible: Boolean) {
        binding?.fragmentKodaBotsWebviewWentWrongWrapper?.visibility =
            if (isVisible) View.VISIBLE else View.GONE
    }

    private fun setupProgress() {
        customConfig?.progressConfig?.backgroundColor?.let {
            binding?.fragmentKodaBotsWebviewRoot?.setBackgroundColor(it)
            binding?.fragmentKodaBotsWebviewProgressWrapper?.setBackgroundColor(it)
        }
        binding?.apply {
            fragmentKodaBotsWebviewProgress.setAnimation(
                customConfig?.progressConfig?.customAnimationPath ?: DEFAULT_LOADER_ASSET
            )
            fragmentKodaBotsWebviewProgress.repeatCount = 0
            fragmentKodaBotsWebviewProgress.renderMode = RenderMode.HARDWARE
        }
        customConfig?.progressConfig?.progressColor?.let { color ->
            binding?.fragmentKodaBotsWebviewProgress?.addValueCallback(
                KeyPath("**"),
                LottieProperty.COLOR_FILTER
            ) {
                PorterDuffColorFilter(
                    color,
                    PorterDuff.Mode.SRC_ATOP
                )
            }
        }
    }

    private fun setupWentWrong() {
        customConfig?.timeoutConfig?.let {
            it.image?.let { image ->
                binding?.fragmentKodaBotsWebviewWentWrongImage?.setImageDrawable(image)
            }
            it.backgroundColor?.let { color ->
                binding?.fragmentKodaBotsWebviewWentWrongWrapper?.setBackgroundColor(color)
            }
            it.buttonText?.let { buttonText ->
                binding?.fragmentKodaBotsWebviewWentWrongButton?.text = buttonText
            }
            if (it.buttonBackgroundDrawable == null) {
                it.buttonColor?.let { buttonColor ->
                    binding?.fragmentKodaBotsWebviewWentWrongButton?.setBackgroundColor(buttonColor)
                }
            } else {
                binding?.fragmentKodaBotsWebviewWentWrongButton?.background =
                    it.buttonBackgroundDrawable
            }
            it.buttonFontSize?.let { buttonFontSize ->
                binding?.fragmentKodaBotsWebviewWentWrongButton?.setTextSize(
                    TypedValue.COMPLEX_UNIT_SP,
                    buttonFontSize
                )
            }
            it.buttonTextColor?.let { buttonTextColor ->
                binding?.fragmentKodaBotsWebviewWentWrongButton?.setTextColor(buttonTextColor)
            }
            it.buttonFont?.let { buttonFont ->
                binding?.fragmentKodaBotsWebviewWentWrongButton?.typeface = buttonFont
            }
            it.message?.let { message ->
                binding?.fragmentKodaBotsWebviewWentWrongMessage?.text = message
            }
            it.messageTextColor?.let { messageTextColor ->
                binding?.fragmentKodaBotsWebviewWentWrongMessage?.setTextColor(messageTextColor)
            }
            it.messageFont?.let { messageFont ->
                binding?.fragmentKodaBotsWebviewWentWrongMessage?.typeface = messageFont
            }
            it.messageFontSize?.let { messageFontSize ->
                binding?.fragmentKodaBotsWebviewWentWrongMessage?.setTextSize(
                    TypedValue.COMPLEX_UNIT_SP,
                    messageFontSize
                )
            }
        }
        binding?.fragmentKodaBotsWebviewWentWrongButton?.setOnClickListener {
            loadUrl()
        }
    }

    private fun setLoadingViewVisibility(isVisible: Boolean) {
        binding?.apply {
            if (isVisible) {
                fragmentKodaBotsWebviewProgress.playAnimation()
                fragmentKodaBotsWebviewProgressWrapper.visibility = View.VISIBLE
            } else {
                fragmentKodaBotsWebviewProgress.pauseAnimation()
                fragmentKodaBotsWebviewProgressWrapper.visibility = View.GONE
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == KodaBotsChromeClient.REQUEST_SELECT_FILE) {
            if (resultCode == Activity.RESULT_OK) {
                chromeClient.filePathCallback?.onReceiveValue(
                    WebChromeClient.FileChooserParams.parseResult(
                        resultCode,
                        data
                    )
                )
            } else {
                chromeClient.filePathCallback?.onReceiveValue(
                    WebChromeClient.FileChooserParams.parseResult(
                        resultCode,
                        null
                    )
                )
            }
        }
    }

    override fun onPause() {
        super.onPause()
        scope.cancel()
        if (isReady) {
            binding?.fragmentKodaBotsWebview?.callJavascript(
                "KodaBots.onPause();"
            )
        }
    }

    override fun onResume() {
        super.onResume()
        scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
        if (isReady) {
            binding?.fragmentKodaBotsWebview?.callJavascript(
                "KodaBots.onResume();"
            )
        }
    }

    private fun initialize() {
        binding?.fragmentKodaBotsWebview?.callJavascript(
            "KodaBots.initialize(${
                if (customConfig?.userProfile != null) Json.encodeToString(
                    UserProfile.serializer(),
                    KodaBotsSDK.gatherPhoneData(requireContext(), customConfig?.userProfile)!!
                ) else null
            }, ${customConfig?.blockId});"
        )
    }

    @JavascriptInterface
    fun onReady(userId: String) {
        timeoutDeferred?.cancel()
        scope.launch(Dispatchers.Main + KodaBotsSDK.globalExceptionHandler) {
            KodaBotsPreferences.userId = userId
            setLoadingViewVisibility(isVisible = false)
            isReady = true
        }
    }

    @JavascriptInterface
    fun onStatEvent(eventType: String, params: String) {
        callbacks.invoke(KodaBotsCallbacks.Event(eventType, params))
        Log.d("KodaBotsSDK", "eventType: $eventType\nparams: $params")
    }

    @JavascriptInterface
    fun onError(error: String) {
        callbacks.invoke(KodaBotsCallbacks.Error(error))
        Log.d("KodaBotsSDK", "eventType: $error")
    }

    @JavascriptInterface
    fun onLinkClicked(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(url)
        })
    }

    /**
     * Method used to send conversation blockId
     *
     * @param blockId Conversation block id
     * @return true if invoked
     */
    fun sendBlock(blockId: String): Boolean {
        return if (isReady) {
            binding?.fragmentKodaBotsWebview?.callJavascript(
                "KodaBots.sentBlock(\"${blockId}\");"
            )
            true
        } else {
            false
        }
    }

    /**
     * Method used to set new user profile
     *
     * @param userProfile new user profile
     * @return true if invoked
     */
    fun syncProfile(userProfile: UserProfile): Boolean {
        return if (isReady) {
            binding?.fragmentKodaBotsWebview?.callJavascript(
                "KodaBots.syncUserProfile(${
                    Json.encodeToString(
                        UserProfile.serializer(),
                        KodaBotsSDK.gatherPhoneData(requireContext(), userProfile)!!
                    )
                });"
            )
            true
        } else {
            false
        }
    }

    /**
     * Method used to simulate error
     *
     * @return true if invoked
     */
    fun simulateError(): Boolean {
        return if (isReady) {
            binding?.fragmentKodaBotsWebview?.callJavascript(
                "KodaBots.simulateError();"
            )
            true
        } else {
            false
        }
    }

    companion object {
        private const val DEFAULT_WENT_WRONG_TIMEOUT = 20L
        private const val DEFAULT_LOADER_ASSET = "default_loader.json"
    }
}

sealed class KodaBotsCallbacks {
    class Event(var type: String, var params: String) : KodaBotsCallbacks()
    class Error(var error: String) : KodaBotsCallbacks()
}