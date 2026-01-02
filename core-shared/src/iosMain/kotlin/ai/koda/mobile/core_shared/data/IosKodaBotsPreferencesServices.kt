package ai.koda.mobile.core_shared.data

import platform.Foundation.NSUserDefaults

class IosKodaBotsPreferencesServices private constructor(

) : KodaBotsPreferencesService {

    companion object {
        val shared = IosKodaBotsPreferencesServices()

        private const val KEY_USER_ID = "KEY_USER_ID"
        private const val PREF_VERSION = "PREF_VERSION"
    }

    private val userPreferences = NSUserDefaults.standardUserDefaults
    override fun initializeEncrypted() {}

    override fun initializeUnsecured() {}

    override fun getPreferencesVersion(): Int {
        return userPreferences.integerForKey(PREF_VERSION).toInt()
    }

    override fun setPreferencesVersion(version: Int) {
        userPreferences.setInteger(version.toLong(), forKey = PREF_VERSION)
    }

    override fun setUserId(value: String?) {
        if (value != null) {
            userPreferences.setObject(value, forKey = KEY_USER_ID)
        } else {
            userPreferences.removeObjectForKey(KEY_USER_ID)
        }
    }

    override fun getUserId(): String? {
        return userPreferences.stringForKey(KEY_USER_ID)
    }
}