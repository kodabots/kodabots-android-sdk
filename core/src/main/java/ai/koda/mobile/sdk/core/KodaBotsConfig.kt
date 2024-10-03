package ai.koda.mobile.sdk.core

import android.graphics.Typeface
import android.graphics.drawable.Drawable

class KodaBotsConfig {
    var userProfile = UserProfile()
    var blockId: String? = null
    var progressConfig: KodaBotsProgressConfig? = null
    var timeoutConfig: KodaBotsTimedOutConfig? = null
}

class KodaBotsProgressConfig {
    var backgroundColor: Int? = null
    var progressColor: Int? = null
    var customAnimationPath: String? = null
}

class KodaBotsTimedOutConfig {
    var timeout: Long? = null
    var image: Drawable? = null
    var backgroundColor: Int? = null
    var buttonText: String? = null
    var buttonColor: Int? = null
    var buttonBackgroundDrawable: Drawable? = null
    var buttonTextColor: Int? = null
    var buttonFont: Typeface? = null
    var buttonFontSize: Float? = null
    var message: String? = null
    var messageTextColor: Int? = null
    var messageFont: Typeface? = null
    var messageFontSize: Float? = null
}
