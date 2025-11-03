@file:OptIn(ExperimentalForeignApi::class)

package ai.koda.mobile.core_shared.presentation

import ai.koda.mobile.core_shared.KodaBotsSDK
import ai.koda.mobile.core_shared.config.AppConfig
import ai.koda.mobile.core_shared.config.KodaBotsConfig
import ai.koda.mobile.core_shared.model.UserProfile
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ExportObjCClass
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import platform.Foundation.NSBundle
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import platform.UIKit.NSLayoutConstraint
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationDidEnterBackgroundNotification
import platform.UIKit.UIApplicationWillEnterForegroundNotification
import platform.UIKit.UIColor
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

        // Setup WebView programowo
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
//        loaderWrapper = UIView().apply {
//            setTranslatesAutoresizingMaskIntoConstraints(false)
//            backgroundColor = UIColor.grayColor
//        }
//        view.addSubview(loaderWrapper!!)
    }

    private fun setupLoaderIndicator() {
//        loaderIndicator = UIView().apply {
//            setTranslatesAutoresizingMaskIntoConstraints(false)
//            backgroundColor = UIColor.clearColor
//        }
//        loaderWrapper?.addSubview(loaderIndicator!!)
    }

    private fun setupWentWrongImage() {
//        wentWrongImage = UIView().apply {
//            setTranslatesAutoresizingMaskIntoConstraints(false)
//            backgroundColor = UIColor.clearColor
//        }
//        wentWrongWrapper?.addSubview(wentWrongImage!!)
    }

    private fun setupWentWrongWrapper() {
//        wentWrongWrapper = UIView().apply {
//            setTranslatesAutoresizingMaskIntoConstraints(false)
//            backgroundColor = UIColor.whiteColor
//            hidden = true
//        }
//        view.addSubview(wentWrongWrapper!!)
//
//        wentWrongLabel = UILabel().apply {
//            setTranslatesAutoresizingMaskIntoConstraints(false)
//            text = "Coś poszło nie tak"
//            textAlignment = NSTextAlignmentCenter
//        }
//        wentWrongWrapper!!.addSubview(wentWrongLabel!!)
//
//        wentWrongButton = UIButton.buttonWithType(UIButtonTypeSystem).apply {
//            setTranslatesAutoresizingMaskIntoConstraints(false)
//            setTitle("Spróbuj ponownie", forState = UIControlStateNormal)
//            addTarget(
//                target = this@IosKodaBotsWebViewScreen,
//                action = sel_registerName("wentWrongButtonClicked"),
//                forControlEvents = UIControlEventTouchUpInside
//            )
//        }
//        wentWrongWrapper!!.addSubview(wentWrongButton!!)
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
//        val timeout = customConfig?.timeoutConfig?.timeout?.toDouble() ?: WENT_WRONG_TIMEOUT
//        wentWrongTimer = NSTimer.scheduledTimerWithTimeInterval(
//            ti = timeout,
//            target = this,
//            selector = sel_registerName("showWentWrong"),
//            userInfo = null,
//            repeats = false
//        )
    }

    // Metody obsługi zdarzeń - bez @ObjCAction
    @Suppress("unused")
    fun wentWrongButtonClicked() {
//        wentWrongWrapper?.hidden = true
//        loaderWrapper?.hidden = false
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

//    @Suppress("unused")
//    fun showWentWrong() {
//        wentWrongWrapper?.hidden = false
//        loaderWrapper?.hidden = true
//    }

    fun sendBlock(blockId: String, params: Map<String, String>? = null): Boolean {
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

// Extension do WKWebView
fun WKWebView.callJavascript(data: String) {
    println("Calling Javascript: $data")
    this.evaluateJavaScript(data, completionHandler = null)
}

