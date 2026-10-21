package com.xapps.utils.xsigner

import android.os.Bundle
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.xapps.utils.xsigner.ui.theme.AppTheme
import com.xapps.utils.xsigner.ui.theme.MonetColorSwatch
import com.xapps.utils.xsigner.ui.theme.MonetThemeController
import com.xapps.utils.xsigner.ui.theme.MonetThemeHost
import com.xapps.utils.xsigner.ui.theme.monetRevealTrigger
import com.xapps.utils.xsigner.ui.widget.ExpressiveFloatingNavBar
import com.xapps.utils.xsigner.ui.widget.FabStyle
import com.xapps.utils.xsigner.ui.widget.NavTabItem

class MainActivity : ComponentActivity() {

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
                    val tabs = remember {
                        listOf(
                            NavTabItem(Icons.Rounded.Home, "Home"),
                            NavTabItem(Icons.Rounded.Key, "Keys"),
                            NavTabItem(Icons.Rounded.Settings, "Settings")
                        )
                    }
                    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        bottomBar = {
                            ExpressiveFloatingNavBar(
                                items = tabs,
                                selectedIndex = selectedTab,
                                onTabSelected = { selectedTab = it },
                                modifier = Modifier
                                    .navigationBarsPadding()
                                    .padding(bottom = 12.dp),
                                showFab = selectedTab == 1,
                                fabStyle = FabStyle.primaryStyle(Icons.Rounded.Add, "Add"),
                                onFabClick = { /* TODO */ }
                            )
                        }
                    ) { innerPadding ->
                        TypographyPreview(
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TypographyPreview(modifier: Modifier = Modifier) {
    val typography = MaterialTheme.typography

    val expressiveSeeds = remember {
        listOf(
            Color(0xFFD0BCFF),
            Color(0xFF00E5FF),
            Color(0xFFFF80AB),
            Color(0xFF76FF03),
            Color(0xFFFFD54F),
            Color(0xFFFF5252),
            Color(0xFF536DFE)
        )
    }

    val styles = listOf(
        "Display Large" to typography.displayLarge,
        "Display Medium" to typography.displayMedium,
        "Display Small" to typography.displaySmall,
        "Headline Large" to typography.headlineLarge,
        "Headline Medium" to typography.headlineMedium,
        "Headline Small" to typography.headlineSmall,
        "Title Large" to typography.titleLarge,
        "Title Medium" to typography.titleMedium,
        "Title Small" to typography.titleSmall,
        "Body Large" to typography.bodyLarge,
        "Body Medium" to typography.bodyMedium,
        "Body Small" to typography.bodySmall,
        "Label Large" to typography.labelLarge,
        "Label Medium" to typography.labelMedium,
        "Label Small" to typography.labelSmall
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "XSigner",
            style = typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.monetRevealTrigger(
                shape = MonetThemeController.RevealShape.SUPERELLIPSE_SQUIRCLE
            ) { controller ->
                controller.setSeedColor(expressiveSeeds.random())
            }
        )

        Text(
            text = "switch theme",
            style = typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.monetRevealTrigger { controller ->
                controller.setDarkMode(!controller.isDark)
            }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MonetColorSwatch(
                seedColor = null,
                size = 56.dp,
                cornerRadius = 16.dp,
                shapeOverride = MonetThemeController.RevealShape.CIRCLE
            )

            expressiveSeeds.forEach { seed ->
                MonetColorSwatch(
                    seedColor = seed,
                    size = 56.dp,
                    cornerRadius = 16.dp,
                    shapeOverride = MonetThemeController.RevealShape.CIRCLE
                )
            }
        }

        HorizontalDivider()

        styles.forEach { (name, style) ->
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = name,
                        style = typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "${style.fontSize.value.toInt()}sp",
                        style = typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "The quick brown fox jumps over the lazy dog.",
                    style = style
                )
            }
        }
    }
}