@file:OptIn(ExperimentalForeignApi::class)

package ai.koda.mobile.core_shared.presentation

import ai.koda.mobile.core_shared.KodaBotsSDK
import ai.koda.mobile.core_shared.config.AppConfig
import ai.koda.mobile.core_shared.config.CustomAnimation
import ai.koda.mobile.core_shared.config.KodaBotsConfig
import ai.koda.mobile.core_shared.model.UserProfile
import cocoapods.lottie_ios.CompatibleAnimation
import cocoapods.lottie_ios.CompatibleAnimationKeypath
import cocoapods.lottie_ios.CompatibleAnimationView
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
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationDidEnterBackgroundNotification
import platform.UIKit.UIApplicationWillEnterForegroundNotification
import platform.UIKit.UIButton
import platform.UIKit.UIButtonTypeSystem
import platform.UIKit.UIColor
import platform.UIKit.UIControlEventTouchUpInside
import platform.UIKit.UIControlStateNormal
import platform.UIKit.UIImageView
import platform.UIKit.UILabel
import platform.UIKit.UIView
import platform.UIKit.UIViewContentMode
import platform.UIKit.UIViewController
import platform.WebKit.WKNavigation
import platform.WebKit.WKNavigationAction
import platform.WebKit.WKNavigationActionPolicy
import platform.WebKit.WKNavigationDelegateProtocol
import platform.WebKit.WKNavigationTypeLinkActivated
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
    private var loaderAnimationView: CompatibleAnimationView? = null
    private var wentWrongWrapper: UIView? = null
    private var wentWrongImage: UIImageView? = null
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
            backgroundColor = customConfig?.progressConfig?.backgroundColor ?: UIColor.grayColor
        }
        view.addSubview(loaderWrapper!!)

        val animation = customConfig?.progressConfig?.customAnimation
            ?: CustomAnimation()

        loaderAnimationView = CompatibleAnimationView(
            compatibleAnimation = CompatibleAnimation(
                name = animation.name,
                subdirectory = animation.subdirectory,
                bundle = animation.bundle
            )
        ).apply {
            setTranslatesAutoresizingMaskIntoConstraints(false)
            setLoopAnimationCount(-1.0)
            customConfig?.progressConfig?.progressColor?.let {
                setColorValue(it, forKeypath = CompatibleAnimationKeypath("**.Color"))
            }
            play()
        }
        loaderWrapper?.addSubview(loaderAnimationView!!)
        val indicatorConstraints = listOf(
            loaderAnimationView!!.centerXAnchor.constraintEqualToAnchor(loaderWrapper!!.centerXAnchor),
            loaderAnimationView!!.centerYAnchor.constraintEqualToAnchor(loaderWrapper!!.centerYAnchor),
            loaderAnimationView!!.widthAnchor.constraintEqualToConstant(64.0),
            loaderAnimationView!!.heightAnchor.constraintEqualToConstant(64.0)
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
            font =
                customConfig?.timeoutConfig?.messageFont ?: platform.UIKit.UIFont.systemFontOfSize(
                    16.0
                )
            textColor = customConfig?.timeoutConfig?.messageTextColor ?: UIColor.redColor
            textAlignment = NSTextAlignmentCenter
        }
        wentWrongWrapper!!.addSubview(wentWrongLabel!!)

        wentWrongButton = UIButton.buttonWithType(UIButtonTypeSystem).apply {
            setTranslatesAutoresizingMaskIntoConstraints(false)
            setTitle(
                customConfig?.timeoutConfig?.buttonText ?: "Try again",
                forState = UIControlStateNormal
            )
            setTitleColor(
                customConfig?.timeoutConfig?.buttonColor ?: UIColor.redColor,
                forState = UIControlStateNormal
            )

            addTarget(
                target = this@IosKodaBotsWebViewScreen,
                action = sel_registerName("wentWrongButtonClicked"),
                forControlEvents = UIControlEventTouchUpInside
            )
        }
        wentWrongWrapper!!.addSubview(wentWrongButton!!)

        wentWrongImage = UIImageView(customConfig?.timeoutConfig?.image).apply {
            setTranslatesAutoresizingMaskIntoConstraints(false)
        }
        wentWrongWrapper?.addSubview(wentWrongImage as UIView)
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

    private fun setupWentWrongConstraints() {
        // Layout for wentWrongWrapper and its children
        wentWrongWrapper?.let { wrapper ->
            println("Setting up constraints for wentWrongWrapper")
            // Create a container view for vertical stacking
            val stackContainer = UIView().apply {
                setTranslatesAutoresizingMaskIntoConstraints(false)
            }
            wrapper.addSubview(stackContainer)

            // Add image, label, and button to the container
            wentWrongImage?.let { image ->
                stackContainer.addSubview(image)
            }
            wentWrongLabel?.let { label ->
                stackContainer.addSubview(label)
            }
            wentWrongButton?.let { button ->
                stackContainer.addSubview(button)
            }

            // Center the container in the wrapper
            val containerConstraints = listOf(
                stackContainer.centerXAnchor.constraintEqualToAnchor(wrapper.centerXAnchor),
                stackContainer.centerYAnchor.constraintEqualToAnchor(wrapper.centerYAnchor),
                stackContainer.leadingAnchor.constraintGreaterThanOrEqualToAnchor(
                    wrapper.leadingAnchor,
                    constant = 32.0
                ),
                stackContainer.trailingAnchor.constraintLessThanOrEqualToAnchor(
                    wrapper.trailingAnchor,
                    constant = -32.0
                )
            )
            NSLayoutConstraint.activateConstraints(containerConstraints)

            // Image constraints
            wentWrongImage?.let { imageView ->
                val imageConstraints = listOf(
                    imageView.topAnchor.constraintEqualToAnchor(stackContainer.topAnchor),
                    imageView.centerXAnchor.constraintEqualToAnchor(stackContainer.centerXAnchor),
                    imageView.widthAnchor.constraintEqualToConstant(200.0),
                    imageView.heightAnchor.constraintEqualToAnchor(imageView.widthAnchor)
                )
                imageView.contentMode = UIViewContentMode.UIViewContentModeScaleAspectFit
                NSLayoutConstraint.activateConstraints(imageConstraints)
            }

            // Label constraints
            wentWrongLabel?.let { label ->
                val labelConstraints = listOf(
                    label.topAnchor.constraintEqualToAnchor(
                        wentWrongImage!!.bottomAnchor,
                        constant = 24.0
                    ),
                    label.centerXAnchor.constraintEqualToAnchor(stackContainer.centerXAnchor),
                    label.leadingAnchor.constraintEqualToAnchor(stackContainer.leadingAnchor),
                    label.trailingAnchor.constraintEqualToAnchor(stackContainer.trailingAnchor)
                )
                NSLayoutConstraint.activateConstraints(labelConstraints)
            }

            // Button constraints
            wentWrongButton?.let { button ->
                val buttonConstraints = listOf(
                    button.topAnchor.constraintEqualToAnchor(
                        wentWrongLabel!!.bottomAnchor,
                        constant = 24.0
                    ),
                    button.centerXAnchor.constraintEqualToAnchor(stackContainer.centerXAnchor),
                    button.widthAnchor.constraintEqualToConstant(120.0),
                    button.heightAnchor.constraintEqualToConstant(44.0),
                    button.bottomAnchor.constraintEqualToAnchor(stackContainer.bottomAnchor)
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
        val clientToken = customConfig?.customClientToken ?: KodaBotsSDK.clientToken ?: ""
        val urlString = "${AppConfig.baseUrl}/mobile/${AppConfig.apiVersion}" +
                "/?token=$clientToken"
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

    @ObjCAction
    fun wentWrongButtonClicked() {
        wentWrongWrapper?.hidden = true
        loaderWrapper?.hidden = false
        loadURL()
    }

    @Suppress("unused")
    @ObjCAction
    fun willEnterForeground() {
        if (isReady) {
            webView?.evaluateJavaScript("KodaBots.onResume();", completionHandler = null)
        }
    }

    @Suppress("unused")
    @ObjCAction
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

    override fun webView(
        webView: WKWebView,
        decidePolicyForNavigationAction: WKNavigationAction,
        decisionHandler: (WKNavigationActionPolicy) -> Unit
    ) {
        val requestUrl = decidePolicyForNavigationAction.request.URL

        if (requestUrl == null) {
            decisionHandler(WKNavigationActionPolicy.WKNavigationActionPolicyAllow)
            return
        }

        // Handle tel: links
        if (requestUrl.scheme == "tel") {
            openUrlSafely(requestUrl)
            decisionHandler(WKNavigationActionPolicy.WKNavigationActionPolicyCancel)
            return
        }

        // Handle external links (target="_blank" or link clicks)
        val shouldOpenExternally =
            decidePolicyForNavigationAction.navigationType == WKNavigationTypeLinkActivated
                    || decidePolicyForNavigationAction.targetFrame == null

        if (shouldOpenExternally) {
            openUrlSafely(requestUrl)
            decisionHandler(WKNavigationActionPolicy.WKNavigationActionPolicyCancel)
            return
        }

        decisionHandler(WKNavigationActionPolicy.WKNavigationActionPolicyAllow)
    }

    private fun openUrlSafely(nsUrl: NSURL) {
        try {
            // Preferred modern API (iOS 10+)
            UIApplication.sharedApplication.openURL(
                nsUrl,
                options = mapOf<Any?, Any?>(),
                completionHandler = null
            )
        } catch (_: Throwable) {
            // Fallback to deprecated API
            UIApplication.sharedApplication.openURL(nsUrl)
        }
    }

    @ObjCAction
    fun showWentWrong() {
        wentWrongWrapper?.hidden = false
        loaderWrapper?.hidden = true
        setupWentWrongConstraints()
    }

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
                        loaderWrapper?.hidden = true
                        wentWrongTimer?.invalidate()
                    }
                }

                "onStatEvent" -> {
                    val eventType = messageBody["eventType"] as? String

                    // TODO: Make this cleaner by fixing serialization issues
                    // Extract params as Map<String, String>, working around type casting issues
                    val params = (messageBody["params"] as? Map<*, *>)?.mapNotNull { (k, v) ->
                        (k as? String)?.let { key -> (v as? String)?.let { value -> key to value } }
                    }?.toMap()
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
                            openUrlSafely(nsUrl)
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
