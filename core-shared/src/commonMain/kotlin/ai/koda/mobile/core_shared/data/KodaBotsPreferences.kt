package ai.koda.mobile.core_shared.data

object KodaBotsPreferences {

    private lateinit var kodaBotsPreferencesService: KodaBotsPreferencesService

    private var preferencesVersion: Int
        get() {
            return kodaBotsPreferencesService.getPreferencesVersion()
        }
        set(version) {
            kodaBotsPreferencesService.setPreferencesVersion(version)
        }

    fun initialize(kodaBotsPreferencesService: KodaBotsPreferencesService) {
        this.kodaBotsPreferencesService = kodaBotsPreferencesService
        this.kodaBotsPreferencesService.initializeEncrypted()
    }

    var userId: String?
        set(value) {
            kodaBotsPreferencesService.setUserId(value)
        }
        get() {
            return kodaBotsPreferencesService.getUserId()
        }

}

//expect fun getKodaBotsPreferencesService(): KodaBotsPreferencesService

interface KodaBotsPreferencesService {
    val PREFERENCES_NAME: String
        get() = "KodaBots.pref"
    val UNSECURED_PREFERENCES_NAME: String
        get() = "KodaBotsUnsecured.pref"
    val PREF_VERSION: String
        get() = "version"
    val KEY_USER_ID: String
        get() = "KEY_USER_ID"


    fun initializeEncrypted()
    fun initializeUnsecured()
    fun getPreferencesVersion(): Int
    fun setPreferencesVersion(version: Int)
    fun setUserId(value: String?)
    fun getUserId(): String?
}