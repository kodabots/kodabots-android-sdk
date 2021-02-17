package com.kodabots.sdk.core

import android.app.Activity
import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.RenderMode
import com.airbnb.lottie.model.KeyPath
import kotlinx.android.synthetic.main.fragment_koda_bots_webview.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class KodaBotsWebViewFragment : Fragment() {
    val chromeClient = KodaBotsChromeClient(this@KodaBotsWebViewFragment)
    var customBackgroundColor: Int? = null
    var customProgressColor: Int? = null
    var userProfile: UserProfile? = null
    var blockId: String? = null
    var customAnimationPath: String? = null
    var callbacks: (KodaBotsCallbacks) -> Unit = {}
    private var isReady = false
    val webviewCallbacks = object : WebviewCallbacks {
        override fun onLoadingFinished() {
            initialize()
        }

        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            return if(request?.url.toString().startsWith("tel", true)){
                startActivity(Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse(request?.url.toString())
                })
                true
            } else {
                false
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.fragment_koda_bots_webview, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        customBackgroundColor?.let {
            fragment_koda_bots_webview_root.setBackgroundColor(it)
            fragment_koda_bots_webview_progress_wrapper.setBackgroundColor(it)
        }
        fragment_koda_bots_webview_progress.setAnimation(
            customAnimationPath ?: "default_loader.json"
        )
        fragment_koda_bots_webview_progress.repeatCount = 0
        fragment_koda_bots_webview_progress.setRenderMode(RenderMode.HARDWARE)
        customProgressColor?.let { color ->
            fragment_koda_bots_webview_progress.addValueCallback(
                KeyPath("**"),
                LottieProperty.COLOR_FILTER,
                {
                    PorterDuffColorFilter(
                        color,
                        PorterDuff.Mode.SRC_ATOP
                    )
                }
            )
        }
        fragment_koda_bots_webview_progress.playAnimation()
        fragment_koda_bots_webview.apply {
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
                setAcceptThirdPartyCookies(fragment_koda_bots_webview, true)
            }
            addJavascriptInterface(this@KodaBotsWebViewFragment, "android")
            webChromeClient = chromeClient
            webViewClient = KodaBotsWebViewClient(webviewCallbacks)
            clearCache(true)
        }.also {
            it.loadUrl("${BuildConfig.BASE_URL}/mobile/${BuildConfig.API_VERSION}/?token=${KodaBotsSDK.clientToken}")
        }
    }

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
        if (isReady) {
            fragment_koda_bots_webview.callJavascript(
                "KodaBots.onPause();"
            )
        }
    }

    override fun onResume() {
        super.onResume()
        if (isReady) {
            fragment_koda_bots_webview.callJavascript(
                "KodaBots.onResume();"
            )
        }
    }

    private fun initialize() {
        fragment_koda_bots_webview.callJavascript(
            "KodaBots.initialize(${
                if (userProfile != null) Json.encodeToString(
                    UserProfile.serializer(),
                    KodaBotsSDK.gatherPhoneData(requireContext(), userProfile)!!
                ) else null
            }, ${blockId});"
        )
    }

    @JavascriptInterface
    fun onReady(userId: String) {
        GlobalScope.launch(Dispatchers.Main + KodaBotsSDK.globalExceptionHandler) {
            KodaBotsPreferences.userId = userId
            fragment_koda_bots_webview_progress.pauseAnimation()
            fragment_koda_bots_webview_progress_wrapper.visibility = View.GONE
            isReady = true
        }
    }

    @JavascriptInterface
    fun onStatEvent(eventType: String, params: String) {
        callbacks.invoke(KodaBotsCallbacks.Event(eventType, params))
    }

    @JavascriptInterface
    fun onError(error: String) {
        callbacks.invoke(KodaBotsCallbacks.Error(error))
    }

    @JavascriptInterface
    fun onLinkClicked(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(url)
        })
    }

    fun sendBlock(blockId: String): Boolean {
        return if (isReady) {
            fragment_koda_bots_webview.callJavascript(
                "KodaBots.sentBlock(\"${blockId}\");"
            )
            true
        } else {
            false
        }
    }

    fun syncProfile(userProfile: UserProfile): Boolean {
        return if (isReady) {
            fragment_koda_bots_webview.callJavascript(
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

    fun simulateError():Boolean {
        return if (isReady) {
            fragment_koda_bots_webview.callJavascript(
                "KodaBots.simulateError();"
            )
            true
        } else {
            false
        }
    }
}

sealed class KodaBotsCallbacks {
    class Event(var type: String, var params: String) : KodaBotsCallbacks()
    class Error(var error: String) : KodaBotsCallbacks()
}