package ai.koda.mobile.core_shared.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class AndroidKodaBotsPreferencesServices(
    private val context: Context
) : KodaBotsPreferencesService {
    private lateinit var preferences: SharedPreferences

    override fun initializeEncrypted() {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            preferences = EncryptedSharedPreferences.create(
                context,
                PREFERENCES_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            //WORKAROUND FOR https://issuetracker.google.com/issues/176215143
            Log.e(
                "KodaBotsSDK",
                "Failed to create encrypted shared preferences ${e::class.java.name} ${e.message}"
            )
            initializeUnsecured()
        }
    }

    override fun initializeUnsecured() {
        preferences = context.getSharedPreferences(UNSECURED_PREFERENCES_NAME, Context.MODE_PRIVATE)
    }

    override fun getPreferencesVersion(): Int =
        preferences.getInt(PREF_VERSION, 1)


    override fun setPreferencesVersion(version: Int) {
        preferences.edit { putInt(PREF_VERSION, version) }
    }

    override fun setUserId(value: String?) {
        preferences.edit { putString(KEY_USER_ID, value) }
    }

    override fun getUserId(): String? {
        return if (preferences.contains(KEY_USER_ID)) preferences.getString(
            KEY_USER_ID,
            null
        ) else null
    }
}

