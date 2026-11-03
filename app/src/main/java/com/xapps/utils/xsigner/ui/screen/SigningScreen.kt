package com.xapps.utils.xsigner.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import com.xapps.utils.xsigner.ui.theme.LocalMonetThemeController
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.rounded.Android
import androidx.compose.material.icons.rounded.FindInPage
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.xapps.utils.xsigner.ui.icon.deployed_code
import com.xapps.utils.xsigner.ui.modifier.rememberBounceOverscrollState
import androidx.compose.ui.graphics.graphicsLayer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun SigningScreen(modifier: Modifier = Modifier) {
    val behavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val expandedStyle = MaterialTheme.typography.headlineLarge
    val collapsedStyle = MaterialTheme.typography.titleLarge

    val overscrollState = rememberBounceOverscrollState()

    Scaffold(
        modifier = modifier
            .nestedScroll(overscrollState.connection)
            .nestedScroll(behavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    val isCollapsedSlot = LocalTextStyle.current.fontSize == MaterialTheme.typography.titleLarge.fontSize

                    Text(
                        text = "XSigner",
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
                ActiveCard(
                    image = Icons.Rounded.Android,
                    title = "Sign an APK file",
                    description = "Select an app from installed apps or pick an APK file from storage",
                    modifier = Modifier
                        .padding(
                            start = 16.dp,
                            end = 16.dp,
                            top = 16.dp
                        )
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Max)
                ) {
                    RegularCard(
                        image = deployed_code,
                        title = "Sign an AAB file",
                        description = "Select an AAB file from storage to sign",
                        modifier = Modifier
                            .padding(start = 16.dp, end = 4.dp, top = 8.dp)
                            .weight(1f)
                            .fillMaxHeight(),
                        buttonText = "Select AAB"
                    )
                    RegularCard(
                        image = Icons.Rounded.FindInPage,
                        title = "Inspect a file",
                        description = "Select an AAB/APK file from storage to inspect",
                        modifier = Modifier
                            .padding(start = 4.dp, end = 16.dp, top = 8.dp)
                            .weight(1f)
                            .fillMaxHeight(),
                        buttonText = "Inspect file"
                    )
                }
            }
        }
    }
}

@Composable
fun ActiveCard(
    image: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    var cca = MaterialTheme.colorScheme.onPrimary
    val isDark = LocalMonetThemeController.current.isDark
    cca = if (isDark) cca.darken(0.5f) else cca.lighten(0.3f)
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = cca,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = image,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp)
                ) {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Text(
                        description,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                    )
                }
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onClick?.invoke() },
            ) {
                Row {
                    Text(
                        "Select APK",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                        contentDescription = null,
                        modifier = Modifier
                            .size(20.dp)
                            .padding(start = 8.dp)
                            .align(Alignment.CenterVertically)
                    )
                }
            }
        }
    }
}

@Composable
fun RegularCard(
    image: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    buttonText: String? = "Button",
    buttonIcon: ImageVector? = null,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = image,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    description,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            TextButton(
                modifier = Modifier
                    .wrapContentSize()
                    .align(Alignment.End),
                onClick = { onClick?.invoke() },
            ) {
                Row {
                    Text(
                        buttonText ?: "Select",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                    Icon(
                        imageVector = buttonIcon ?: Icons.AutoMirrored.Rounded.ArrowForwardIos,
                        contentDescription = null,
                        modifier = Modifier
                            .size(20.dp)
                            .padding(start = 8.dp)
                            .align(Alignment.CenterVertically)
                    )
                }
            }
        }
    }
}

fun Color.darken(factor: Float): Color =
    copy(
        red = red * (1f - factor),
        green = green * (1f - factor),
        blue = blue * (1f - factor)
    )

fun Color.lighten(factor: Float): Color =
    copy(
        red = red + ((1f - red) * factor),
        green = green + ((1f - green) * factor),
        blue = blue + ((1f - blue) * factor),
    )
