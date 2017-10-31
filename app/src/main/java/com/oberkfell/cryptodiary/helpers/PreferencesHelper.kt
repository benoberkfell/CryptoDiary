package com.oberkfell.cryptodiary.helpers

import android.app.Application.MODE_PRIVATE
import android.content.SharedPreferences
import android.util.Base64
import com.oberkfell.cryptodiary.activity.CryptoDiaryApplication
import javax.inject.Inject
import javax.inject.Singleton

class PreferencesHelper @Inject constructor(application: CryptoDiaryApplication){

    private val PREF_FILE_NAME = "CryptoDiaryPrefs"

    private val PREF_HAS_BUILT_REALM = "has_built_realm"
    private val PREF_SALT = "salt"
    private val PREF_ENCRYPTED_REALM_KEY = "realm_key"
    private val PREF_IV = "iv"

    private val preferences: SharedPreferences

    var hasBuiltRealm: Boolean
        get() {
            return getBooleanPref(PREF_HAS_BUILT_REALM)
        }
        set(value) {
            return setBooleanPref(PREF_HAS_BUILT_REALM, value)
        }

    var salt: ByteArray
        get() {
            return getByteArrayPref(PREF_SALT)
        }
        set(value) {
            return setByteArrayPref(PREF_SALT, value)
        }

    var encryptedRealmKey: ByteArray
        get() {
            return getByteArrayPref(PREF_ENCRYPTED_REALM_KEY)
        }
        set(value) {
            return setByteArrayPref(PREF_ENCRYPTED_REALM_KEY, value)
        }

    var iv: ByteArray
        get() {
            return getByteArrayPref(PREF_IV)
        }
        set(value) {
            return setByteArrayPref(PREF_IV, value)
        }

    init {
        preferences = application.getSharedPreferences(PREF_FILE_NAME, MODE_PRIVATE)
    }

    fun isSaltSet() : Boolean {
        return !getStringPref(PREF_SALT).isEmpty()
    }

    fun isKeySet() : Boolean {
        return !getStringPref(PREF_ENCRYPTED_REALM_KEY).isEmpty()
    }

    fun clearKey() {
        preferences.edit().remove(PREF_ENCRYPTED_REALM_KEY)
                .remove(PREF_IV)
                .apply()
    }

    private fun getByteArrayPref(prefName: String) : ByteArray {
        return Base64.decode(preferences.getString(prefName, ""), Base64.DEFAULT)
    }

    private fun setByteArrayPref(prefName: String, value: ByteArray) {
        setStringPref(prefName, Base64.encodeToString(value, Base64.DEFAULT))
    }


    private fun getStringPref(prefName: String) : String {
        return preferences.getString(prefName, "")
    }

    private fun setStringPref(prefName: String, value: String) {
        preferences.edit().putString(prefName, value).apply()
    }

    private fun getBooleanPref(prefName: String) : Boolean {
        return preferences.getBoolean(prefName, false)
    }

    private fun setBooleanPref(prefName: String, value: Boolean) {
        preferences.edit().putBoolean(prefName, value).apply()
    }
}