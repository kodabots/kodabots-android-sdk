package ai.koda.mobile.core_shared.config

import platform.UIKit.UIColor
import platform.UIKit.UIFont
import platform.UIKit.UIImage

// Placeholder classes for types until I come up with the idea how to use platform specific implementations
// from iOS libraries
class UserProfile
class LottieAnimation

class KodaBotsConfig(
    var userProfile: UserProfile = UserProfile(),
    var blockId: String? = null,
    var progressConfig: KodaBotsProgressConfig? = null,
    var timeoutConfig: KodaBotsTimedOutConfig? = null
)

class KodaBotsProgressConfig(
    var backgroundColor: UIColor? = null,
    var progressColor: UIColor? = null,
    var customAnimation: LottieAnimation? = null
)

class KodaBotsTimedOutConfig(
    var timeout: Int? = null,
    var image: UIImage? = null,
    var backgroundColor: UIColor? = null,
    var buttonText: String? = null,
    var buttonColor: UIColor? = null,
    var buttonTextColor: UIColor? = null,
    var buttonFont: UIFont? = null,
    var buttonFontSize: Float? = null,
    var buttonCornerRadius: Float? = null,
    var buttonBorderWidth: Float? = null,
    var buttonBorderColor: UIColor? = null,
    var message: String? = null,
    var messageTextColor: UIColor? = null,
    var messageFont: UIFont? = null,
    var messageFontSize: Float? = null
)