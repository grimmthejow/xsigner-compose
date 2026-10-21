package com.xapps.utils.xsigner.ui.theme

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

class ThemePreferences(context: Context) {
    private val prefs = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)

    var seedColor: Color?
        get() {
            val colorInt = prefs.getInt("seed_color", 0)
            return if (colorInt == 0) null else Color(colorInt)
        }
        set(value) {
            if (value == null) {
                prefs.edit().remove("seed_color").apply()
            } else {
                prefs.edit().putInt("seed_color", value.toArgb()).apply()
            }
        }

    fun isDarkMode(default: Boolean): Boolean {
        return prefs.getBoolean("is_dark_mode", default)
    }

    fun setDarkMode(isDark: Boolean) {
        prefs.edit().putBoolean("is_dark_mode", isDark).apply()
    }
}
