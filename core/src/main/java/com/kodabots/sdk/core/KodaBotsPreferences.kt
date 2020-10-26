package com.kodabots.sdk.core

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

object KodaBotsPreferences {
    private lateinit var preferences: SharedPreferences
    private val PREFERENCES_NAME = "KodaBots.pref"
    private val PREF_VERSION = "version"

    private var preferencesVersion: Int
        get() = preferences.getInt(PREF_VERSION, 1)
        set(version) = preferences.edit().putInt(PREF_VERSION, version).apply()

    fun initialize(context: Context) {
        val masterKeyAlias: String = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        preferences = EncryptedSharedPreferences.create(
            PREFERENCES_NAME,
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private val KEY_USER_ID = "KEY_USER_ID"
    var userId: String?
        set(value) {
            preferences.edit().putString(KEY_USER_ID, value).apply()
        }
        get() {
            return if (preferences.contains(KEY_USER_ID)) preferences.getString(
                KEY_USER_ID,
                null
            ) else null
        }

}
