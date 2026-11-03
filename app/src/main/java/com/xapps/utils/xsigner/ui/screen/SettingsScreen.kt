package com.xapps.utils.xsigner.ui.screen

import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.xapps.utils.xsigner.pref.LocalRuntimePrefs
import com.xapps.utils.xsigner.ui.modifier.rememberBounceOverscrollState
import com.xapps.utils.xsigner.ui.theme.LocalMonetThemeController
import com.xapps.utils.xsigner.ui.theme.MonetColorSwatch
import com.xapps.utils.xsigner.ui.widget.ExpressiveSettingsList
import com.xapps.utils.xsigner.ui.widget.SettingGroupSpec
import com.xapps.utils.xsigner.ui.widget.SettingItemSpec

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun SettingsScreen(modifier: Modifier = Modifier) {
    val behavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val expandedStyle = MaterialTheme.typography.headlineLarge
    val collapsedStyle = MaterialTheme.typography.titleLarge

    val overscrollState = rememberBounceOverscrollState()

    val controller = LocalMonetThemeController.current
    val runtimePrefs = LocalRuntimePrefs.current
    val selectedThemeIndex = runtimePrefs.themeMode

    val themeOptions = listOf("System", "Light", "Dark")

    val generatedSwatches = remember {
        listOf(
            Color(0xFFD0BCFF),
            Color(0xFF00E5FF),
            Color(0xFFFF80AB),
            Color(0xFF76FF03),
            Color(0xFFFFD54F),
            Color(0xFFFF5252),
            Color(0xFF536DFE),
            Color(0xFF9C27B0),
            Color(0xFF009688),
            Color(0xFF607D8B)
        )
    }

    val appearanceItems = mutableListOf<SettingItemSpec>()

    appearanceItems.add(
        SettingItemSpec.Custom(
            id = "theme_mode",
            title = "Theme Mode",
            description = "Choose primary app theme",
            content = {
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    themeOptions.forEachIndexed { index, label ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = themeOptions.size),
                            onClick = {
                                controller.selectThemeMode(
                                    newMode = index,
                                    tapOriginInRoot = Offset.Zero
                                )
                            },
                            selected = selectedThemeIndex == index
                        ) {
                            Text(label)
                        }
                    }
                }
            }
        )
    )

    appearanceItems.add(
        SettingItemSpec.Switch(
            id = "dynamic_colors",
            title = "Dynamic Colors",
            description = "Use system colors or custom accent palette",
            checked = runtimePrefs.isDynamicColorEnabled,
            onCheckedChange = { enabled ->
                controller.setDynamicColorEnabled(enabled)
            }
        )
    )

    if (runtimePrefs.isDynamicColorEnabled) {
        appearanceItems.add(
            SettingItemSpec.Custom(
                id = "color_swatches",
                content = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MonetColorSwatch(
                            seedColor = null,
                            size = 48.dp,
                            cornerRadius = 16.dp
                        )

                        VerticalDivider(
                            modifier = Modifier.height(36.dp).padding(horizontal = 4.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )

                        generatedSwatches.forEach { seed ->
                            MonetColorSwatch(
                                seedColor = seed,
                                size = 48.dp,
                                cornerRadius = 16.dp
                            )
                        }

                        MonetColorSwatch(
                            seedColor = Color(0xFF616161),
                            size = 48.dp,
                            cornerRadius = 16.dp
                        )
                    }
                }
            )
        )
    }

    val settingsGroups = listOf(
        SettingGroupSpec(
            id = "appearance",
            title = "Appearance",
            items = appearanceItems
        ),
        SettingGroupSpec(
            id = "signing",
            title = "Signing",
            items = listOf(
                SettingItemSpec.Navigation(
                    id = "default_scheme",
                    title = "Default signature scheme",
                    description = "Manage the default used signature scheme when signing",
                    valueText = "",
                    onClick = { }
                ),
                SettingItemSpec.Switch(
                    id = "prompt_scheme",
                    title = "Prompt for signature scheme",
                    description = "Disable to automatically use the default set scheme without user prompt",
                    checked = runtimePrefs.zipalignEnabled,
                    onCheckedChange = { runtimePrefs.zipalignEnabled = it }
                ),
                SettingItemSpec.Switch(
                    id = "verify_file",
                    title = "Verify after signing",
                    description = "Automatically verify the signed APK after signing for any issues",
                    checked = runtimePrefs.verifyFileEnabled,
                    onCheckedChange = { runtimePrefs.verifyFileEnabled = it }
                ),
                SettingItemSpec.Switch(
                    id = "delete_after_signing",
                    title = "Delete file after signing",
                    description = "Automatically delete the input file from storage after signing",
                    checked = runtimePrefs.deleteAfterSigningEnabled,
                    onCheckedChange = { runtimePrefs.deleteAfterSigningEnabled = it }
                ),
                SettingItemSpec.Navigation(
                    id = "output_folder",
                    title = "Output Directory",
                    description = "Choose where signed files are saved",
                    valueText = "",
                    onClick = { }
                ),
                SettingItemSpec.Split(
                    id = "output_suffix",
                    title = "Add suffix for signed files",
                    description = "Toggle and manage adding suffix to the signed files names",
                    checked = runtimePrefs.outputSuffixEnabled,
                    onCheckedChange = { runtimePrefs.outputSuffixEnabled = it },
                    onDetailClick = { }
                )
            )
        ),
        SettingGroupSpec(
            id = "security_privacy",
            title = "Security & Privacy",
            items = listOf(
                SettingItemSpec.Switch(
                    id = "biometric_auth",
                    title = "Biometric Authentication",
                    description = "Require Authentication before using store keystore credentials",
                    checked = runtimePrefs.biometricAuthenticationEnabled,
                    onCheckedChange = { runtimePrefs.biometricAuthenticationEnabled = it }
                ),
                SettingItemSpec.Switch(
                    id = "flag_secure",
                    title = "Use secure window flags",
                    description = "Prevent the app content from appearing in screenshots and screen records",
                    checked = runtimePrefs.flagSecureEnabled,
                    onCheckedChange = { runtimePrefs.flagSecureEnabled = it }
                ),
                SettingItemSpec.Navigation(
                    id = "delete_credentials",
                    title = "Delete saved credentials",
                    description = "Delete all saved keystore credentials from the app data folder",
                    valueText = "",
                    onClick = { }
                )
            )
        ),
        SettingGroupSpec(
            id = "advanced",
            title = "Advanced",
            items = listOf(
                SettingItemSpec.Navigation(
                    id = "export_logs",
                    title = "Export app logs",
                    description = "Share app logs to help investigate issues",
                    valueText = "",
                    onClick = { }
                ),
                SettingItemSpec.Navigation(
                    id = "clear_data",
                    title = "Clear all app data",
                    description = "This will delete all stored keystores and their saved credentials, and reset all the app prefs",
                    valueText = "",
                    onClick = { }
                )
            )
        ),
        SettingGroupSpec(
            id = "about",
            title = "About",
            items = listOf(
                SettingItemSpec.Navigation(
                    id = "about_xsigner",
                    title = "About XSigner",
                    description = "All you need to know about XSigner",
                    valueText = "",
                    onClick = { }
                )
            )
        )
    )

    Scaffold(
        modifier = modifier
            .nestedScroll(overscrollState.connection)
            .nestedScroll(behavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    val isCollapsedSlot = LocalTextStyle.current.fontSize == MaterialTheme.typography.titleLarge.fontSize

                    Text(
                        text = "Settings",
                        style = if (isCollapsedSlot) collapsedStyle else expandedStyle,
                    )
                },
                scrollBehavior = behavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        }
    ) { innerPadding ->
        CompositionLocalProvider(
            LocalOverscrollFactory provides null
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding())
                    .graphicsLayer {
                        if (overscrollState.overscrollOffset.value > 0) {
                            translationY = overscrollState.overscrollOffset.value
                        }
                    }
                    .clip(
                        RoundedCornerShape(
                            topStart = 24.dp, topEnd = 24.dp
                        )
                    )
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .graphicsLayer {
                        if (overscrollState.overscrollOffset.value < 0) {
                            translationY = overscrollState.overscrollOffset.value
                        }
                    }
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 100.dp)
            ) {
                ExpressiveSettingsList(
                    groups = settingsGroups,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}
