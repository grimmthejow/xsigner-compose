package com.xapps.utils.xsigner

import android.os.Bundle
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.xapps.utils.xsigner.ui.screen.RootScreen
import com.xapps.utils.xsigner.ui.theme.AppTheme
import com.xapps.utils.xsigner.ui.theme.MonetThemeHost

class RootActivity : ComponentActivity() {

    companion object {
        private var isColdStart = true
    }

    private var splashTimeout = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null && isColdStart) {
            isColdStart = false
            splashTimeout = SystemClock.uptimeMillis() + 750
        }
        splashScreen.setKeepOnScreenCondition {
            SystemClock.uptimeMillis() < splashTimeout
        }

        enableEdgeToEdge()
        setContent {
            AppTheme {
                MonetThemeHost {
                    RootScreen()
                }
            }
        }
    }
}