package com.xapps.utils.xsigner.pref

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.edit

class PrefsManager(context: Context) {
    private val prefs = context.getSharedPreferences("xsigner_prefs", Context.MODE_PRIVATE)

    var themeMode: Int
        get() = prefs.getInt("theme_mode", 0)
        set(value) = prefs.edit { putInt("theme_mode", value) }

    var seedColor: Color?
        get() {
            val colorInt = prefs.getInt("seed_color", 0)
            return if (colorInt == 0) null else Color(colorInt)
        }
        set(value) {
            if (value == null) {
                prefs.edit { remove("seed_color") }
            } else {
                prefs.edit { putInt("seed_color", value.toArgb()) }
            }
        }

    var isDynamicColorEnabled: Boolean
        get() = prefs.getBoolean("dynamic_color", true)
        set(value) = prefs.edit { putBoolean("dynamic_color", value) }

    var zipalignEnabled: Boolean
        get() = prefs.getBoolean("zipalign_enabled", true)
        set(value) = prefs.edit { putBoolean("zipalign_enabled", value) }

    var verifyFileEnabled: Boolean
        get() = prefs.getBoolean("verify_file_enabled", true)
        set(value) = prefs.edit { putBoolean("verify_file_enabled", value) }

    var deleteAfterSigningEnabled: Boolean
        get() = prefs.getBoolean("delete_after_signing_enabled", true)
        set(value) = prefs.edit { putBoolean("delete_after_signing_enabled", value) }


    var outputDestination: String
        get() = prefs.getString("output_destination", "Downloads/XSigner") ?: "Downloads/XSigner"
        set(value) = prefs.edit { putString("output_destination", value) }

    var outputSuffixEnabled: Boolean
        get() = prefs.getBoolean("output_suffix_enabled", false)
        set(value) = prefs.edit { putBoolean("output_suffix_enabled", value) }

    var outputSuffix: String
        get() = prefs.getString("output_suffix", "-signed") ?: "-signed"
        set(value) = prefs.edit { putString("output_suffix", value) }

    var biometricAuthenticationEnabled: Boolean
        get() = prefs.getBoolean("biometric_authentication_enabled", false)
        set(value) = prefs.edit { putBoolean("biometric_authentication_enabled", value) }

    var flagSecureEnabled: Boolean
        get() = prefs.getBoolean("flag_secure_enabled", false)
        set(value) = prefs.edit { putBoolean("flag_secure_enabled", value) }
}
