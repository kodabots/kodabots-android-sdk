package ai.koda.mobile.sdk.core

import android.graphics.Typeface
import android.graphics.drawable.Drawable

/**
 * Configuration class for KodaBots SDK.
 * Contains user profile, block ID, progress configuration, timeout configuration,
 * camera permission info, and API configuration.
 *
 * Do not overwrite values if you want to use default ones.
 */
data class KodaBotsConfig(
    val userProfile: UserProfile = UserProfile(),
    val blockId: String? = null,
    val progressConfig: KodaBotsProgressConfig? = null,
    val timeoutConfig: KodaBotsTimedOutConfig? = null,
    val noCameraPermissionInfo: String? = null,
    val apiConfig: KodaBotsApiConfig? = null,
)

data class KodaBotsProgressConfig(
    val backgroundColor: Int? = null,
    val progressColor: Int? = null,
    val customAnimationPath: String? = null,
)

data class KodaBotsTimedOutConfig(
    /**
     * Timeout in seconds. If null no timeout will be applied.
     * Default timeout is 20 seconds.
     */
    val timeout: Long? = 20L,
    val image: Drawable? = null,
    val backgroundColor: Int? = null,
    val buttonText: String? = null,
    val buttonColor: Int? = null,
    val buttonBackgroundDrawable: Drawable? = null,
    val buttonTextColor: Int? = null,
    val buttonFont: Typeface? = null,
    val buttonFontSize: Float? = null,
    val message: String? = null,
    val messageTextColor: Int? = null,
    val messageFont: Typeface? = null,
    val messageFontSize: Float? = null,
)

data class KodaBotsApiConfig(
    val baseUrl: String? = null,
    val authToken: String? = null,
)
