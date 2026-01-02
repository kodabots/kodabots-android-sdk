package ai.koda.mobile.core_shared.config

import ai.koda.mobile.core_shared.model.UserProfile
import platform.UIKit.UIColor
import platform.UIKit.UIFont
import platform.UIKit.UIImage


class KodaBotsConfig(
    var userProfile: UserProfile = UserProfile(),
    var blockId: String? = null,
    var progressConfig: KodaBotsProgressConfig? = null,
    var timeoutConfig: KodaBotsTimedOutConfig? = null,
    var customClientId: String? = null,
)

class KodaBotsProgressConfig(
    var backgroundColor: UIColor? = null,
    var progressColor: UIColor? = null,
)

class KodaBotsTimedOutConfig {
    var timeout: Int? = null
    var image: UIImage? = null
    var backgroundColor: UIColor? = null
    var buttonText: String? = null
    var buttonColor: UIColor? = null
    var buttonTextColor: UIColor? = null
    var buttonFont: UIFont? = null
    var buttonFontSize: Float? = null
    var buttonCornerRadius: Float? = null
    var buttonBorderWidth: Float? = null
    var buttonBorderColor: UIColor? = null
    var message: String? = null
    var messageTextColor: UIColor? = null
    var messageFont: UIFont? = null
    var messageFontSize: Float? = null
}