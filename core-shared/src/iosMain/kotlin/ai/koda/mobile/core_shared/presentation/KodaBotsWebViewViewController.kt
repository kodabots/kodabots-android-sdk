@file:OptIn(ExperimentalForeignApi::class)

package ai.koda.mobile.core_shared.presentation

import ai.koda.mobile.core_shared.KodaBotsSDK
import ai.koda.mobile.core_shared.config.AppConfig
import ai.koda.mobile.core_shared.config.KodaBotsConfig
import ai.koda.mobile.core_shared.model.UserProfile
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ExportObjCClass
import kotlinx.cinterop.ObjCAction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import platform.Foundation.NSBundle
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSTimer
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import platform.UIKit.NSLayoutConstraint
import platform.UIKit.NSTextAlignmentCenter
import platform.UIKit.UIActivityIndicatorView
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationDidEnterBackgroundNotification
import platform.UIKit.UIApplicationWillEnterForegroundNotification
import platform.UIKit.UIButton
import platform.UIKit.UIButtonTypeSystem
import platform.UIKit.UIColor
import platform.UIKit.UIControlEventTouchUpInside
import platform.UIKit.UIControlStateNormal
import platform.UIKit.UILabel
import platform.UIKit.UIView
import platform.UIKit.UIViewController
import platform.WebKit.WKNavigation
import platform.WebKit.WKNavigationDelegateProtocol
import platform.WebKit.WKScriptMessage
import platform.WebKit.WKScriptMessageHandlerProtocol
import platform.WebKit.WKUserContentController
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration
import platform.objc.sel_registerName

@OptIn(BetaInteropApi::class)
@ExportObjCClass
class IosKodaBotsWebViewScreen
@OverrideInit constructor(
    nibName: String? = null,
    bundle: NSBundle? = null
) : UIViewController(nibName, bundle),
    WKScriptMessageHandlerProtocol, WKNavigationDelegateProtocol {

    private var webView: WKWebView? = null
    private var loaderWrapper: UIView? = null
    private var loaderIndicator: UIActivityIndicatorView? = null
    private var wentWrongWrapper: UIView? = null
    private var wentWrongImage: UIView? = null
    private var wentWrongLabel: UILabel? = null
    private var wentWrongButton: UIButton? = null

    private val WENT_WRONG_TIMEOUT = 20.0
    private var wentWrongTimer: NSTimer? = null
    private var isReady = false

    var customConfig: KodaBotsConfig? = null
    var callbacks: (KodaBotsCallback) -> Unit = { }
        get() = field

    override fun viewDidLoad() {
        super.viewDidLoad()
        setup()
    }

    override fun viewWillDisappear(animated: Boolean) {
        super.viewWillDisappear(animated)
        if (isReady) {
            webView?.evaluateJavaScript("KodaBots.onPause();", completionHandler = null)
        }
        webView?.setNavigationDelegate(null)
    }

    private fun setup() {
        setupObservers()
        setupViews()
        setupWebView()
        loadURL()
    }

    private fun setupObservers() {
        NSNotificationCenter.defaultCenter.addObserver(
            observer = this,
            selector = sel_registerName("willEnterForeground"),
            name = UIApplicationWillEnterForegroundNotification,
            `object` = null
        )

        NSNotificationCenter.defaultCenter.addObserver(
            observer = this,
            selector = sel_registerName("handleAppDidEnterBackground"),
            name = UIApplicationDidEnterBackgroundNotification,
            `object` = null
        )
    }

    private fun setupViews() {
        view.backgroundColor = UIColor.whiteColor

        val webConfiguration = WKWebViewConfiguration()
        webView = WKWebView(frame = view.bounds, configuration = webConfiguration).apply {
            setTranslatesAutoresizingMaskIntoConstraints(false)
            setNavigationDelegate(this@IosKodaBotsWebViewScreen)
        }
        view.addSubview(webView!!)

        setupLoaderWrapper()
        setupWentWrongWrapper()
        setupConstraints()
    }

    private fun setupLoaderWrapper() {
        loaderWrapper = UIView().apply {
            setTranslatesAutoresizingMaskIntoConstraints(false)
            backgroundColor = UIColor.grayColor
        }
        view.addSubview(loaderWrapper!!)

        loaderIndicator = UIActivityIndicatorView().apply {
            setTranslatesAutoresizingMaskIntoConstraints(false)
            startAnimating()
            customConfig?.progressConfig?.progressColor?.let {
                color = it
            }
        }
        loaderWrapper?.addSubview(loaderIndicator!!)

        // Center the indicator in the loaderWrapper
        val indicatorConstraints = listOf(
            loaderIndicator!!.centerXAnchor.constraintEqualToAnchor(loaderWrapper!!.centerXAnchor),
            loaderIndicator!!.centerYAnchor.constraintEqualToAnchor(loaderWrapper!!.centerYAnchor),
            loaderIndicator!!.widthAnchor.constraintEqualToConstant(40.0),
            loaderIndicator!!.heightAnchor.constraintEqualToConstant(40.0)
        )
        NSLayoutConstraint.activateConstraints(indicatorConstraints)

        // Fill the loaderWrapper to overlay the main view
        val wrapperConstraints = listOf(
            loaderWrapper!!.topAnchor.constraintEqualToAnchor(view.safeAreaLayoutGuide.topAnchor),
            loaderWrapper!!.leadingAnchor.constraintEqualToAnchor(view.leadingAnchor),
            loaderWrapper!!.trailingAnchor.constraintEqualToAnchor(view.trailingAnchor),
            loaderWrapper!!.bottomAnchor.constraintEqualToAnchor(view.bottomAnchor)
        )
        NSLayoutConstraint.activateConstraints(wrapperConstraints)
    }

    private fun setupWentWrongWrapper() {
        wentWrongWrapper = UIView().apply {
            setTranslatesAutoresizingMaskIntoConstraints(false)
            backgroundColor = customConfig?.timeoutConfig?.backgroundColor ?: UIColor.whiteColor
            hidden = true
        }
        view.addSubview(wentWrongWrapper!!)

        wentWrongLabel = UILabel().apply {
            setTranslatesAutoresizingMaskIntoConstraints(false)
            text = customConfig?.timeoutConfig?.message ?: "Something went wrong."
            textAlignment = NSTextAlignmentCenter
        }
        wentWrongWrapper!!.addSubview(wentWrongLabel!!)

        wentWrongButton = UIButton.buttonWithType(UIButtonTypeSystem).apply {
            setTranslatesAutoresizingMaskIntoConstraints(false)
            setTitle(
                customConfig?.timeoutConfig?.buttonText ?: "Try again",
                forState = UIControlStateNormal
            )
            addTarget(
                target = this@IosKodaBotsWebViewScreen,
                action = sel_registerName("wentWrongButtonClicked"),
                forControlEvents = UIControlEventTouchUpInside
            )
        }
        wentWrongWrapper!!.addSubview(wentWrongButton!!)

        wentWrongImage = UIView().apply {
            setTranslatesAutoresizingMaskIntoConstraints(false)
            backgroundColor = UIColor.clearColor
        }
        wentWrongWrapper?.addSubview(wentWrongImage!!)
    }

    // There constraints are set for webView and other views programmatically
    private fun setupConstraints() {
        webView?.let { webView ->
            val constraints = listOf(
                webView.topAnchor.constraintEqualToAnchor(view.safeAreaLayoutGuide.topAnchor),
                webView.leadingAnchor.constraintEqualToAnchor(view.leadingAnchor),
                webView.trailingAnchor.constraintEqualToAnchor(view.trailingAnchor),
                webView.bottomAnchor.constraintEqualToAnchor(view.bottomAnchor)
            )
            NSLayoutConstraint.activateConstraints(constraints)
        }

        // Layout for wentWrongWrapper and its children
        wentWrongWrapper?.let { wrapper ->
            wentWrongImage?.let { image ->
                val imageConstraints = listOf(
                    image.centerXAnchor.constraintEqualToAnchor(wrapper.centerXAnchor),
                    image.topAnchor.constraintEqualToAnchor(wrapper.topAnchor, constant = 80.0),
                    image.widthAnchor.constraintEqualToConstant(80.0),
                    image.heightAnchor.constraintEqualToConstant(80.0)
                )
                NSLayoutConstraint.activateConstraints(imageConstraints)
            }
            wentWrongLabel?.let { label ->
                val labelConstraints = listOf(
                    label.centerXAnchor.constraintEqualToAnchor(wrapper.centerXAnchor),
                    label.topAnchor.constraintEqualToAnchor(wentWrongImage!!.bottomAnchor, constant = 24.0),
                    label.leadingAnchor.constraintEqualToAnchor(wrapper.leadingAnchor, constant = 32.0),
                    label.trailingAnchor.constraintEqualToAnchor(wrapper.trailingAnchor, constant = -32.0)
                )
                NSLayoutConstraint.activateConstraints(labelConstraints)
            }
            wentWrongButton?.let { button ->
                val buttonConstraints = listOf(
                    button.centerXAnchor.constraintEqualToAnchor(wrapper.centerXAnchor),
                    button.topAnchor.constraintEqualToAnchor(wentWrongLabel!!.bottomAnchor, constant = 24.0),
                    button.widthAnchor.constraintEqualToConstant(120.0),
                    button.heightAnchor.constraintEqualToConstant(44.0)
                )
                NSLayoutConstraint.activateConstraints(buttonConstraints)
            }
            // Fill the wrapper to the view
            val wrapperConstraints = listOf(
                wrapper.topAnchor.constraintEqualToAnchor(view.safeAreaLayoutGuide.topAnchor),
                wrapper.leadingAnchor.constraintEqualToAnchor(view.leadingAnchor),
                wrapper.trailingAnchor.constraintEqualToAnchor(view.trailingAnchor),
                wrapper.bottomAnchor.constraintEqualToAnchor(view.bottomAnchor)
            )
            NSLayoutConstraint.activateConstraints(wrapperConstraints)
        }
    }

    private fun setupWebView() {
        webView?.configuration?.userContentController?.let { controller ->
            controller.addScriptMessageHandler(this, name = "onReady")
            controller.addScriptMessageHandler(this, name = "onStatEvent")
            controller.addScriptMessageHandler(this, name = "onError")
            controller.addScriptMessageHandler(this, name = "onLinkClicked")
        }
    }

    private fun loadURL() {
        val urlString = "${AppConfig.baseUrl}/mobile/${AppConfig.apiVersion}" +
                "/?token=${KodaBotsSDK.clientToken}"
        println("KodaBotsWebView: Loading URL: $urlString")
        NSURL.URLWithString(urlString)?.let { nsUrl ->
            val request = NSURLRequest.requestWithURL(nsUrl)
            webView?.loadRequest(request)
            startWentWrongTimer()
        }
    }

    private fun startWentWrongTimer() {
        val timeout = customConfig?.timeoutConfig?.timeout?.toDouble() ?: WENT_WRONG_TIMEOUT
        wentWrongTimer = NSTimer.scheduledTimerWithTimeInterval(
            ti = timeout,
            target = this,
            selector = sel_registerName("showWentWrong"),
            userInfo = null,
            repeats = false
        )
    }

    @Suppress("unused")
    fun wentWrongButtonClicked() {
        wentWrongWrapper?.hidden = true
        loaderWrapper?.hidden = false
        loadURL()
    }

    @Suppress("unused")
    fun willEnterForeground() {
        if (isReady) {
            webView?.evaluateJavaScript("KodaBots.onResume();", completionHandler = null)
        }
    }

    @Suppress("unused")
    fun handleAppDidEnterBackground() {
        if (isReady) {
            webView?.evaluateJavaScript("KodaBots.onPause();", completionHandler = null)
        }
    }

    override fun webView(webView: WKWebView, didFinishNavigation: WKNavigation?) {
        MainScope().launch(Dispatchers.Main) {
            delay(1000)
            initialize()
        }
    }

    fun sendBlock(blockId: String, params: Map<String, String>? = null): Boolean {
    @ObjCAction
    fun showWentWrong() {
        wentWrongWrapper?.hidden = false
        loaderWrapper?.hidden = true
    }

        if (!isReady) return false
        val jsCode = if (params.isNullOrEmpty()) {
            "KodaBots.sentBlock(\"$blockId\");"
        } else {
            val jsonParams = Json.encodeToString(params)
            "KodaBots.sentBlock(\"$blockId\",$jsonParams);"
        }
        webView?.evaluateJavaScript(jsCode, completionHandler = null)
        return true
    }

    fun syncUserProfile(profile: UserProfile): Boolean {
        if (isReady) {
            try {
                val jsonString = Json.encodeToString(UserProfile.serializer(), profile)
                webView?.callJavascript("KodaBots.syncUserProfile($jsonString);")
                return true
            } catch (e: Exception) {
                return false
            }
        }
        return false
    }

    fun simulateError(): Boolean {
        if (isReady) {
            webView?.callJavascript("KodaBots.simulateError();")
            return true
        }
        return false
    }

    fun initialize() {
        if (customConfig?.userProfile != null && customConfig?.blockId != null) {
            try {
                val userProfileData = KodaBotsSDK.gatherPhoneData(customConfig?.userProfile!!)
                val jsonString = userProfileData?.let { Json.encodeToString(it) } ?: ""
                val blockIdString = customConfig?.blockId ?: ""
                webView?.callJavascript("KodaBots.initialize($jsonString,$blockIdString);")
            } catch (e: Exception) {
                val blockIdString = customConfig?.blockId ?: ""
                webView?.callJavascript("KodaBots.initialize(null,$blockIdString);")
            }
        } else if (customConfig?.userProfile != null && customConfig?.blockId == null) {
            try {
                val userProfileData = KodaBotsSDK.gatherPhoneData(customConfig?.userProfile!!)
                val jsonString = userProfileData?.let { Json.encodeToString(it) } ?: ""
                webView?.callJavascript("KodaBots.initialize($jsonString,null);")
            } catch (e: Exception) {
                webView?.callJavascript("KodaBots.initialize(null,null);")
            }
        } else if (customConfig?.userProfile == null && customConfig?.blockId != null) {
            val blockIdString = customConfig?.blockId ?: ""
            webView?.callJavascript("KodaBots.initialize(null,$blockIdString);")
        } else {
            webView?.callJavascript("KodaBots.initialize(null,null);")
        }
    }

    override fun userContentController(
        userContentController: WKUserContentController,
        didReceiveScriptMessage: WKScriptMessage
    ) {
        val messageName = didReceiveScriptMessage.name
        val body = didReceiveScriptMessage.body as? Map<String, Any?>
        body?.let { messageBody ->
            when (messageName) {
                "onReady" -> {
                    val userId = messageBody["userId"] as? String
                    if (userId != null) {
                        isReady = true
                        loaderWrapper?.hidden = true
                        wentWrongTimer?.invalidate()
                    }
                }

                "onStatEvent" -> {
                    val eventType = messageBody["eventType"] as? String
                    val params = messageBody["params"] as? Map<String, String>
                    if (eventType != null && params != null) {
                        callbacks(KodaBotsEvent(eventType, params))
                    }
                }

                "onError" -> {
                    val error = messageBody["error"] as? String
                    if (error != null) {
                        callbacks(KodaBotsError(error))
                    }
                }

                "onLinkClicked" -> {
                    val urlString = messageBody["url"] as? String
                    urlString?.let { url ->
                        NSURL.URLWithString(url)?.let { nsUrl ->
                            UIApplication.sharedApplication.openURL(nsUrl)
                        }
                    }
                }
            }
        }
    }
}

fun WKWebView.callJavascript(data: String) {
    println("Calling Javascript: $data")
    this.evaluateJavaScript(data, completionHandler = null)
}
