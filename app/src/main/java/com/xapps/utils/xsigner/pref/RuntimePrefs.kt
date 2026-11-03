package com.xapps.utils.xsigner.pref

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

@Stable
class RuntimePrefs(
    private val prefsManager: PrefsManager
) {
    private val _themeMode = mutableIntStateOf(prefsManager.themeMode)
    var themeMode: Int
        get() = _themeMode.intValue
        set(value) {
            if (_themeMode.intValue == value) return
            _themeMode.intValue = value
            prefsManager.themeMode = value
        }

    private val _seedColor = mutableStateOf(prefsManager.seedColor)
    var seedColor: Color?
        get() = _seedColor.value
        set(value) {
            if (_seedColor.value == value) return
            _seedColor.value = value
            prefsManager.seedColor = value
        }

    private val _isDynamicColorEnabled = mutableStateOf(prefsManager.isDynamicColorEnabled)
    var isDynamicColorEnabled: Boolean
        get() = _isDynamicColorEnabled.value
        set(value) {
            if (_isDynamicColorEnabled.value == value) return
            _isDynamicColorEnabled.value = value
            prefsManager.isDynamicColorEnabled = value
        }

    private val _zipalignEnabled = mutableStateOf(prefsManager.zipalignEnabled)
    var zipalignEnabled: Boolean
        get() = _zipalignEnabled.value
        set(value) {
            if (_zipalignEnabled.value == value) return
            _zipalignEnabled.value = value
            prefsManager.zipalignEnabled = value
        }

    private val _verifyFileEnabled = mutableStateOf(prefsManager.verifyFileEnabled)
    var verifyFileEnabled: Boolean
        get() = _verifyFileEnabled.value
        set(value) {
            if (_verifyFileEnabled.value == value) return
            _verifyFileEnabled.value = value
            prefsManager.verifyFileEnabled = value
        }

    private val _deleteAfterSigningEnabled = mutableStateOf(prefsManager.deleteAfterSigningEnabled)
    var deleteAfterSigningEnabled: Boolean
        get() = _deleteAfterSigningEnabled.value
        set(value) {
            if (_deleteAfterSigningEnabled.value == value) return
            _deleteAfterSigningEnabled.value = value
            prefsManager.deleteAfterSigningEnabled = value
        }

    private val _outputDestination = mutableStateOf(prefsManager.outputDestination)
    var outputDestination: String
        get() = _outputDestination.value
        set(value) {
            if (_outputDestination.value == value) return
            _outputDestination.value = value
            prefsManager.outputDestination = value
        }

    private val _outputSuffixEnabled = mutableStateOf(prefsManager.outputSuffixEnabled)
    var outputSuffixEnabled: Boolean
        get() = _outputSuffixEnabled.value
        set(value) {
            if (_outputSuffixEnabled.value == value) return
            _outputSuffixEnabled.value = value
            prefsManager.outputSuffixEnabled = value
        }

    private val _outputSuffix = mutableStateOf(prefsManager.outputSuffix)
    var outputSuffix: String
        get() = _outputSuffix.value
        set(value) {
            if (_outputSuffix.value == value) return
            _outputSuffix.value = value
            prefsManager.outputSuffix = value
        }

    private val _biometricAuthenticationEnabled = mutableStateOf(prefsManager.biometricAuthenticationEnabled)
    var biometricAuthenticationEnabled: Boolean
        get() = _biometricAuthenticationEnabled.value
        set(value) {
            if (_biometricAuthenticationEnabled.value == value) return
            _biometricAuthenticationEnabled.value = value
            prefsManager.biometricAuthenticationEnabled = value
        }

    private val _flagSecureEnabled = mutableStateOf(prefsManager.flagSecureEnabled)
    var flagSecureEnabled: Boolean
        get() = _flagSecureEnabled.value
        set(value) {
            if (_flagSecureEnabled.value == value) return
            _flagSecureEnabled.value = value
            prefsManager.flagSecureEnabled = value
        }
}

val LocalRuntimePrefs = staticCompositionLocalOf<RuntimePrefs> {
    error("RuntimePrefs not initialized. Wrap UI in ProvideRuntimePrefs or CompositionLocalProvider.")
}

@Composable
fun rememberRuntimePrefs(): RuntimePrefs {
    val context = LocalContext.current
    return remember(context) {
        RuntimePrefs(PrefsManager(context))
    }
}

@Composable
fun ProvideRuntimePrefs(
    runtimePrefs: RuntimePrefs = rememberRuntimePrefs(),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalRuntimePrefs provides runtimePrefs) {
        content()
    }
}
