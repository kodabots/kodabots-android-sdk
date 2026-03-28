package ai.koda.mobile.core_shared.presentation

import ai.koda.mobile.core_shared.AndroidKodaBotsSDKDriver
import ai.koda.mobile.core_shared.BuildConfig
import ai.koda.mobile.core_shared.KodaBotsSDK
import ai.koda.mobile.core_shared.R
import ai.koda.mobile.core_shared.config.AppConfig
import ai.koda.mobile.core_shared.config.KodaBotsConfig
import ai.koda.mobile.core_shared.data.KodaBotsPreferences
import ai.koda.mobile.core_shared.databinding.FragmentKodaBotsWebviewBinding
import ai.koda.mobile.core_shared.model.UserProfile
import ai.koda.mobile.core_shared.screen.KodaBotsWebViewScreen
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.RenderMode
import com.airbnb.lottie.model.KeyPath
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.io.File
import java.util.concurrent.TimeUnit

class KodaBotsWebViewFragment : Fragment(R.layout.fragment_koda_bots_webview), FileChooserLauncher,
    KodaBotsWebViewScreen {
    private var binding: FragmentKodaBotsWebviewBinding? = null
    private val chromeClient =
        KodaBotsChromeClient(
            this@KodaBotsWebViewFragment,
            this@KodaBotsWebViewFragment,
            requestPermissionIfNeeded = ::requestPermissionIfNeeded
        )
    private var isReady = false
    private var timeoutDeferred: Deferred<Unit>? = null
    private var imageIntentPickerIntentToLaunch: Intent? = null
    private var tempFile: File? = null

    private var pendingRequestPermission: KodaBotsChromeClient.WebPermissionRequest? = null

    var customConfig: KodaBotsConfig? = KodaBotsConfig()
    var callbacks: (KodaBotsCallbacks) -> Unit = {}

    private val clientToken: String
        get() = customConfig?.customClientToken ?: KodaBotsSDK.clientToken ?: ""

    private val kodaBotUrl
        get() = "${AppConfig.baseUrl}/mobile/${AppConfig.apiVersion}" +
                "/?token=$clientToken"

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
                    data = request?.url.toString().toUri()
                })
                true
            } else {
                startActivity(Intent(Intent.ACTION_VIEW).apply {
                    data = request?.url ?: Uri.EMPTY
                })
                true
            }
        }
    }

    private val requestCameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        startFileChooserIntent(isGranted)
        if (!isGranted) {
            noCameraPermission()
        }
    }

    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionToGrantedMap ->
        pendingRequestPermission?.let { webRequestPermission ->
            if (permissionToGrantedMap.filter { it.value }
                    .map { it.key }.toTypedArray().isNotEmpty())
                chromeClient.onPermissionGranted(
                    webRequestPermission,
                )
            else
                chromeClient.onPermissionDenied(webRequestPermission)
        }
    }

    private val startForFileChooserResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            handleFileChooserActivityResult(result)
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
                this.allowContentAccess = true
                this.allowFileAccess = true
                this.loadWithOverviewMode = true
                this.useWideViewPort = true
                this.domStorageEnabled = true
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
        try {
            PhotoUtils(requireContext()).clearCache()
        } catch (e: Exception) {
            Log.e("KodaBotsSDK", e.message, e)
        }
        super.onDestroyView()
    }

    private fun loadUrl() {
        setLoadingViewVisibility(isVisible = true)
        setErrorViewVisibility(isVisible = false)
        Log.d("KodaBots", kodaBotUrl)
        binding?.fragmentKodaBotsWebview?.loadUrl(kodaBotUrl)
        timeoutDeferred =
            viewLifecycleOwner.lifecycleScope.async(Dispatchers.Main + getGlobalExceptionHandler()) {
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

    override fun onPause() {
        super.onPause()
        if (isReady) {
            binding?.fragmentKodaBotsWebview?.callJavascript(
                "KodaBots.onPause();"
            )
        }
    }

    override fun onResume() {
        super.onResume()
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
                    KodaBotsSDK.gatherPhoneData(
                        customConfig?.userProfile
                    )!!
                ) else null
            }, ${customConfig?.blockId});"
        )
    }

    @JavascriptInterface
    fun onReady(userId: String) {
        timeoutDeferred?.cancel()
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main + getGlobalExceptionHandler()) {
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
     * @param params Additional custom parameters
     * @return true if invoked
     */
    fun sendBlock(blockId: String, params: Map<String, String>? = null): Boolean {
        return params?.let {
            if (params.isNotEmpty()) {
                val paramsJson = Json.encodeToString(params)
                return if (isReady) {
                    binding?.fragmentKodaBotsWebview?.callJavascript(
                        "KodaBots.sentBlock(\"${blockId}\", ${paramsJson});"
                    )
                    true
                } else {
                    false
                }
            } else null
        } ?: sendBlock(blockId)
    }

    private fun sendBlock(blockId: String): Boolean {
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
                        KodaBotsSDK.gatherPhoneData(userProfile)!!
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

    // If camera permission is declared in application it have to be handled
    // https://developer.android.com/reference/android/provider/MediaStore#ACTION_IMAGE_CAPTURE
    override fun launchFileChooser(
        intent: Intent
    ) {
        imageIntentPickerIntentToLaunch = intent
        if (isCameraPermissionDeclared()) {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            startFileChooserIntent()
        }
    }

    private fun isCameraPermissionDeclared() = context?.packageManager?.getPackageInfo(
        context?.applicationContext!!.packageName,
        PackageManager.GET_PERMISSIONS
    )?.requestedPermissions?.contains(Manifest.permission.CAMERA) ?: false

    private fun startFileChooserIntent(isCameraPermissionGranted: Boolean = true) {
        val intent = Intent.createChooser(
            imageIntentPickerIntentToLaunch,
            null
        )
        if (isCameraPermissionGranted) {
            tempFile = PhotoUtils(requireContext()).createTempFile()
            startForFileChooserResult.launch(intent.apply {
                putExtra(
                    Intent.EXTRA_INITIAL_INTENTS,
                    arrayOf(Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                        tempFile?.let {
                            putExtra(
                                MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(
                                    requireContext(),
                                    "${context?.applicationContext?.packageName}.koda.sdk.provider",
                                    it
                                )
                            )
                        }

                    })
                )
            })
        } else {
            startForFileChooserResult.launch(intent)
        }
        imageIntentPickerIntentToLaunch = null
    }

    private fun noCameraPermission() {
        customConfig?.noCameraPermissionInfo?.let {
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleFileChooserActivityResult(result: ActivityResult) {
        if (result.resultCode == Activity.RESULT_OK) {
            if (result.data?.extras != null) {
                // If photo is not saved to file then thumbnail is returned
                // This condition is kind of fallback, but is not preferred to be used
                val uri = getBitmapFromIntentExtras(result.data?.extras)
                chromeClient.filePathCallback?.onReceiveValue(
                    uri?.let { arrayOf(it) }
                )
            } else if (result.data?.data != null) {
                chromeClient.filePathCallback?.onReceiveValue(
                    WebChromeClient.FileChooserParams.parseResult(
                        result.resultCode,
                        result.data
                    )
                )
            } else {
                chromeClient.filePathCallback?.onReceiveValue(
                    tempFile?.toUri()?.let { arrayOf(it) }
                )
            }
        } else {
            chromeClient.filePathCallback?.onReceiveValue(
                WebChromeClient.FileChooserParams.parseResult(
                    result.resultCode,
                    null
                )
            )
        }
        tempFile = null
    }

    private fun getBitmapFromIntentExtras(extras: Bundle?) = sdk33OrHigher {
        extras?.getParcelable("data", Bitmap::class.java)?.let {
            PhotoUtils(requireContext()).handleBitmap(it)
        }
    } ?: extras?.getParcelable<Bitmap>("data")?.let {
        PhotoUtils(requireContext()).handleBitmap(it)
    }

    private fun requestPermissionIfNeeded(
        request: KodaBotsChromeClient.WebPermissionRequest
    ) {
        pendingRequestPermission = request
        requestPermissionsLauncher.launch(request.getNativePermissionToRequest())
    }

    private fun getGlobalExceptionHandler() =
        (KodaBotsSDK.driver as? AndroidKodaBotsSDKDriver)?.globalExceptionHandler
            ?: kotlinx.coroutines.CoroutineExceptionHandler { _, exception ->
                Log.e("KodaBotsSDK", "Coroutine exception: ${exception.message}")
            }

    companion object {
        private const val DEFAULT_WENT_WRONG_TIMEOUT = 20L
        private const val DEFAULT_LOADER_ASSET = "default_loader.json"
    }
}

interface FileChooserLauncher {
    fun launchFileChooser(
        intent: Intent
    )
}

sealed class KodaBotsCallbacks {
    class Event(var type: String, var params: String) : KodaBotsCallbacks()
    class Error(var error: String) : KodaBotsCallbacks()
}
