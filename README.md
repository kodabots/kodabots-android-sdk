# KodaBots SDK

A Kotlin Multiplatform SDK for integrating KodaBots AI-powered chatbot into your Android and iOS applications.

## Installation

### Android

#### 1. Add Maven Repository

Add the KodaBots Maven repository to your `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()

        // Add KodaBots Maven repository
        maven {
            url = uri("https://europe-maven.pkg.dev/kodaai-mobile-sdk/kotlin-sdk")
        }
    }
}
```

#### 2. Add Dependency

Add the dependency to your app-level `build.gradle.kts`:

```kotlin
dependencies {
    // For Ktor 2.x or other networking libraries
    implementation("ai.koda.mobile.sdk:koda-core:<latest_version>")

    // For Ktor 3.x
    implementation("ai.koda.mobile.sdk:koda-core-ktor3:<latest_version>")
}
```

#### 3. Configure AndroidManifest

Add your client token to `AndroidManifest.xml`:

```xml
<application>
    <!-- Your other configuration -->

    <meta-data
        android:name="ai.koda.mobile.sdk.ClientToken"
        android:value="YOUR_CLIENT_TOKEN_HERE"/>
</application>
```

### iOS

#### 1. Add XCFramework to Xcode

The SDK generates an XCFramework called `KodaBotsKit`. Add it to your Xcode project:

1. Build the XCFramework:
   ```bash
   ./gradlew :core-shared:assembleKodaBotsKitXCFramework
   ```

2. The framework will be located at:
   ```
   core-shared/build/XCFrameworks/release/KodaBotsKit.xcframework
   ```

3. Drag and drop `KodaBotsKit.xcframework` into your Xcode project

4. In your target's settings, ensure the framework is added to "Frameworks, Libraries, and Embedded Content"

#### 2. Configure Info.plist

Add your client token to `Info.plist`:

```xml
<key>KodaBotsClientToken</key>
<string>YOUR_CLIENT_TOKEN_HERE</string>
```

### Kotlin Multiplatform (KMP) Apps

If you're integrating this SDK into a Kotlin Multiplatform app with a shared module, you **must** use `api()` instead of `implementation()` in your shared module's dependencies.

#### Why `api()` is Required

The SDK provides platform-specific UI components (Fragment for Android, UIViewController for iOS) that your native apps need to access directly. Using `api()` makes these types transitively available to your Android and iOS apps.

#### Setup

Add the dependency to your shared module's `build.gradle.kts`:

```kotlin
// shared/build.gradle.kts
kotlin {
    sourceSets {
        commonMain {
            dependencies {
                // IMPORTANT: Use api(), not implementation()
                api("ai.koda.mobile.sdk:koda-core:<latest_version>")

                // For Ktor 3.x projects
                api("ai.koda.mobile.sdk:koda-core-ktor3:<latest_version>")
            }
        }
    }
}
```

This allows your native apps to:
- Access `KodaBotsSDK` directly
- Use `KodaBotsWebViewFragment` (Android)
- Use the generated `UIViewController` (iOS)
- Configure `KodaBotsConfig`, `UserProfile`, and other SDK types

**Note:** If you use `implementation()` instead, your native apps won't have access to the SDK's types, and you'll need to create wrapper functions in your shared module.

## Quick Start

### Android

#### 1. Initialize SDK in Application Class

```kotlin
class MyApplication : Application() {

    private val callbacks: (KodaBotsCallbacks) -> Unit = { callback ->
        when (callback) {
            is KodaBotsCallbacks.Event -> {
                Log.d("KodaBots", "Event: ${callback.type}, Params: ${callback.params}")
            }
            is KodaBotsCallbacks.Error -> {
                Log.e("KodaBots", "Error: ${callback.error}")
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        val config = KodaBotsConfig().apply {
            progressConfig = KodaBotsProgressConfig().apply {
                progressColor = Color.RED
                backgroundColor = Color.WHITE
            }
            noCameraPermissionInfo = "Camera permission is required to take photos."
        }

        val success = KodaBotsSDK.init(
            AndroidKodaBotsSDKDriver(
                context = this,
                callbacks = callbacks,
                config = config
            )
        )

        if (!success) {
            Log.e("KodaBots", "SDK initialization failed")
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        KodaBotsSDK.uninitialize()
    }
}
```

#### 2. Display Chatbot Fragment

```kotlin
class MainActivity : AppCompatActivity() {

    private var kodaBotsFragment: KodaBotsWebViewFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Generate the chatbot fragment
        kodaBotsFragment = KodaBotsSDK.generateScreen() as? KodaBotsWebViewFragment

        // Display in a container
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_container, kodaBotsFragment!!)
            commit()
        }
    }
}
```

### iOS

#### 1. Initialize SDK

```swift
import UIKit
import KodaBotsKit

@main
class AppDelegate: UIResponder, UIApplicationDelegate {

    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
    ) -> Bool {

        let config = KodaBotsConfig(
            userProfile: UserProfile(),
            blockId: nil,
            progressConfig: KodaBotsProgressConfig(
                backgroundColor: UIColor.white,
                progressColor: UIColor.red
            ),
            timeoutConfig: nil,
            customClientId: nil
        )

        let driver = IosKodaBotsSDKDriver(
            config: config,
            callbacks: { callback in
                if let event = callback as? KodaBotsEvent {
                    print("Event: \(event.type), Params: \(event.params)")
                } else if let error = callback as? KodaBotsError {
                    print("Error: \(error.error)")
                }
            }
        )

        let success = KodaBotsSDK.shared.init(driver: driver)
        if !success {
            print("SDK initialization failed")
        }

        return true
    }
}
```

#### 2. Display Chatbot View Controller

```swift
import UIKit
import KodaBotsKit

class ViewController: UIViewController {

    override func viewDidLoad() {
        super.viewDidLoad()

        // Generate the chatbot view controller
        if let chatVC = KodaBotsSDK.shared.generateScreen() as? UIViewController {
            addChild(chatVC)
            view.addSubview(chatVC.view)

            // Setup constraints
            chatVC.view.translatesAutoresizingMaskIntoConstraints = false
            NSLayoutConstraint.activate([
                chatVC.view.topAnchor.constraint(equalTo: view.topAnchor),
                chatVC.view.bottomAnchor.constraint(equalTo: view.bottomAnchor),
                chatVC.view.leadingAnchor.constraint(equalTo: view.leadingAnchor),
                chatVC.view.trailingAnchor.constraint(equalTo: view.trailingAnchor)
            ])

            chatVC.didMove(toParent: self)
        }
    }
}
```

## Configuration

### KodaBotsConfig

#### Android

```kotlin
class KodaBotsConfig {
    var userProfile = UserProfile()              // User profile data
    var blockId: String? = null                  // Initial conversation block ID
    var progressConfig: KodaBotsProgressConfig?  // Loading screen customization
    var timeoutConfig: KodaBotsTimedOutConfig?   // Timeout screen customization
    var noCameraPermissionInfo: String?          // Message when camera permission denied
    var customClientId: String?                  // Override client token
}
```

#### iOS

```kotlin
class KodaBotsConfig(
    var userProfile: UserProfile = UserProfile(),
    var blockId: String? = null,
    var progressConfig: KodaBotsProgressConfig? = null,
    var timeoutConfig: KodaBotsTimedOutConfig? = null,
    var customClientId: String? = null
)
```

### KodaBotsProgressConfig

Configure the loading screen appearance.

#### Android

```kotlin
KodaBotsProgressConfig().apply {
    backgroundColor = Color.WHITE           // Background color
    progressColor = Color.RED               // Progress indicator color
    customAnimationPath = "custom_anim.json" // Path to custom Lottie animation
}
```

#### iOS

```swift
KodaBotsProgressConfig(
    backgroundColor: UIColor.white,         // Background color
    progressColor: UIColor.red              // Progress indicator color
)
```

### KodaBotsTimedOutConfig

Configure the timeout error screen.

#### Android

```kotlin
KodaBotsTimedOutConfig().apply {
    timeout = 20000L                        // Timeout in milliseconds
    image = ContextCompat.getDrawable(context, R.drawable.error_image)
    backgroundColor = Color.WHITE
    buttonText = "Retry"
    buttonColor = Color.BLUE
    buttonTextColor = Color.WHITE
    buttonFont = Typeface.DEFAULT_BOLD
    buttonFontSize = 16f
    message = "Connection timed out. Please try again."
    messageTextColor = Color.BLACK
    messageFont = Typeface.DEFAULT
    messageFontSize = 14f
}
```

#### iOS

```swift
let timeoutConfig = KodaBotsTimedOutConfig()
timeoutConfig.timeout = 20                  // Timeout in seconds
timeoutConfig.image = UIImage(named: "error_image")
timeoutConfig.backgroundColor = UIColor.white
timeoutConfig.buttonText = "Retry"
timeoutConfig.buttonColor = UIColor.blue
timeoutConfig.buttonTextColor = UIColor.white
timeoutConfig.buttonFont = UIFont.boldSystemFont(ofSize: 16)
timeoutConfig.buttonFontSize = 16.0
timeoutConfig.buttonCornerRadius = 8.0
timeoutConfig.buttonBorderWidth = 1.0
timeoutConfig.buttonBorderColor = UIColor.blue
timeoutConfig.message = "Connection timed out. Please try again."
timeoutConfig.messageTextColor = UIColor.black
timeoutConfig.messageFont = UIFont.systemFont(ofSize: 14)
timeoutConfig.messageFontSize = 14.0
```

### UserProfile

User profile data to sync with the chatbot.

```kotlin
val userProfile = UserProfile().apply {
    first_name = "John"
    last_name = "Doe"
    email = "john.doe@example.com"

    // Add custom parameters
    custom_parameters["user_id"] = "12345"
    custom_parameters["subscription_tier"] = "premium"
}
```

Device information (OS, model, manufacturer, locale) is automatically collected by the SDK.

## API Reference

### KodaBotsSDK (Singleton)

Main entry point for the SDK.

```kotlin
object KodaBotsSDK {
    // Initialize the SDK
    fun init(driver: KodaBotsSDKDriver): Boolean

    // Check if SDK is initialized
    val isInitialized: Boolean

    // Get/Set client token
    var clientToken: String?

    // Gather device information
    fun gatherPhoneData(userProfile: UserProfile? = null): UserProfile?

    // Get unread message count (callback-based)
    fun getUnreadCount(callback: (CallResponse<Int?>) -> Unit)

    // Get unread message count (suspend-based)
    suspend fun getUnreadCount(): CallResponse<Int?>

    // Generate platform-specific UI component
    fun generateScreen(): Any?  // Returns Fragment (Android) or UIViewController (iOS)

    // Cleanup resources
    fun uninitialize()
}
```

### KodaBotsWebViewFragment (Android)

Methods available on the chatbot fragment.

```kotlin
class KodaBotsWebViewFragment : Fragment {
    // Send a conversation block with parameters
    fun sendBlock(blockId: String, params: Map<String, String>? = null): Boolean

    // Sync user profile with chatbot
    fun syncProfile(userProfile: UserProfile): Boolean

    // Simulate an error (for testing)
    fun simulateError(): Boolean
}
```

### IosKodaBotsWebViewScreen (iOS)

Methods available on the chatbot view controller.

```kotlin
class IosKodaBotsWebViewScreen {
    // Send a conversation block with parameters
    fun sendBlock(blockId: String, params: Map<String, String>? = null): Boolean

    // Sync user profile with chatbot
    fun syncUserProfile(profile: UserProfile): Boolean

    // Simulate an error (for testing)
    fun simulateError(): Boolean

    // Initialize the view controller
    fun initialize()
}
```

### CallResponse<T>

Result type for API calls.

```kotlin
sealed class CallResponse<T> {
    class Success<T>(val value: T) : CallResponse<T>()
    class Error<T>(val exception: Exception) : CallResponse<T>()
    class Timeout<T> : CallResponse<T>()
}
```

## Usage Examples

### Get Unread Message Count

#### Android (Coroutine)

```kotlin
lifecycleScope.launch {
    when (val result = KodaBotsSDK.getUnreadCount()) {
        is CallResponse.Success -> {
            val count = result.value
            Log.d("KodaBots", "Unread count: $count")
        }
        is CallResponse.Error -> {
            Log.e("KodaBots", "Error: ${result.exception.message}")
        }
        is CallResponse.Timeout -> {
            Log.e("KodaBots", "Request timed out")
        }
    }
}
```

#### Android (Callback)

```kotlin
KodaBotsSDK.getUnreadCount { result ->
    when (result) {
        is CallResponse.Success -> {
            val count = result.value
            Log.d("KodaBots", "Unread count: $count")
        }
        is CallResponse.Error -> {
            Log.e("KodaBots", "Error: ${result.exception.message}")
        }
        is CallResponse.Timeout -> {
            Log.e("KodaBots", "Request timed out")
        }
    }
}
```

#### iOS

```swift
KodaBotsSDK.shared.getUnreadCount { result in
    if let success = result as? CallResponseSuccess<NSNumber> {
        let count = success.value
        print("Unread count: \(count)")
    } else if let error = result as? CallResponseError<NSNumber> {
        print("Error: \(error.exception.localizedDescription)")
    } else if result is CallResponseTimeout<NSNumber> {
        print("Request timed out")
    }
}
```

### Sync User Profile

#### Android

```kotlin
val userProfile = UserProfile().apply {
    first_name = "Jane"
    last_name = "Smith"
    email = "jane.smith@example.com"
    custom_parameters["department"] = "Engineering"
}

val success = kodaBotsFragment?.syncProfile(userProfile)
if (success == true) {
    Log.d("KodaBots", "Profile synced successfully")
}
```

#### iOS

```swift
let userProfile = UserProfile()
userProfile.first_name = "Jane"
userProfile.last_name = "Smith"
userProfile.email = "jane.smith@example.com"
userProfile.custom_parameters["department"] = "Engineering"

if let chatScreen = kodaBotsScreen as? IosKodaBotsWebViewScreen {
    let success = chatScreen.syncUserProfile(profile: userProfile)
    if success {
        print("Profile synced successfully")
    }
}
```

### Send Conversation Block

#### Android

```kotlin
val params = mapOf(
    "product_id" to "12345",
    "action" to "view_details"
)

val success = kodaBotsFragment?.sendBlock("product_block", params)
if (success == true) {
    Log.d("KodaBots", "Block sent successfully")
}
```

#### iOS

```swift
let params = [
    "product_id": "12345",
    "action": "view_details"
]

if let chatScreen = kodaBotsScreen as? IosKodaBotsWebViewScreen {
    let success = chatScreen.sendBlock(blockId: "product_block", params: params)
    if success {
        print("Block sent successfully")
    }
}
```

## Callbacks

### Android

Handle events and errors from the chatbot:

```kotlin
private val callbacks: (KodaBotsCallbacks) -> Unit = { callback ->
    when (callback) {
        is KodaBotsCallbacks.Event -> {
            // Handle chatbot events
            val eventType = callback.type
            val eventParams = callback.params  // JSON string

            Log.d("KodaBots", "Event: $eventType")
            Log.d("KodaBots", "Params: $eventParams")
        }
        is KodaBotsCallbacks.Error -> {
            // Handle errors
            Log.e("KodaBots", "Error: ${callback.error}")
        }
    }
}
```

### iOS

```swift
let callbacks: (KodaBotsCallback) -> Void = { callback in
    if let event = callback as? KodaBotsEvent {
        // Handle chatbot events
        let eventType = event.type
        let eventParams = event.params

        print("Event: \(eventType)")
        print("Params: \(eventParams)")
    } else if let error = callback as? KodaBotsError {
        // Handle errors
        print("Error: \(error.error)")
    }
}
```

## Advanced Features

### Custom Client Token

Override the client token from manifest/Info.plist:

```kotlin
KodaBotsConfig().apply {
    customClientId = "custom_token_here"
}
```

### Custom Loading Animation (Android)

Use a custom Lottie animation for the loading screen:

```kotlin
KodaBotsProgressConfig().apply {
    customAnimationPath = "custom_loading_animation.json"
}
```

Place the Lottie JSON file in your `assets` folder.

## Credits

Default loading animation: [Loader by Sinan Özkök](https://lottiefiles.com/36219-loader)
