package com.xapps.utils.xsigner.ui.widget

import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastFold
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.util.fastMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

data class NavTabItem(
    val icon: ImageVector,
    val label: String,
    val contentDescription: String = label
)

data class FabStyle(
    val icon: ImageVector,
    val contentDescription: String,
    val containerColor: Color,
    val contentColor: Color,
    val shape: Shape = CircleShape,
    val sizeDp: Dp = 56.dp,
    val elevationDp: Dp = 6.dp
) {
    companion object {
        @Composable
        fun defaultStyle(icon: ImageVector, contentDescription: String) = FabStyle(
            icon = icon,
            contentDescription = contentDescription,
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface
        )

        @Composable
        fun secondaryStyle(icon: ImageVector, contentDescription: String) = FabStyle(
            icon = icon,
            contentDescription = contentDescription,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            shape = CircleShape,
            sizeDp = 56.dp
        )

        @Composable
        fun primaryStyle(icon: ImageVector, contentDescription: String) = FabStyle(
            icon = icon,
            contentDescription = contentDescription,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = RoundedCornerShape(16.dp),
            sizeDp = 64.dp
        )
    }
}

data class BarStyle(
    val barContainerColor: Color,
    val selectedTabContainerColor: Color,
    val unselectedTabContainerColor: Color,
    val selectedTabContentColor: Color,
    val unselectedTabContentColor: Color,
    val shape: Shape = CircleShape,
    val maxWidthDp: Dp = 348.dp,
    val minTabWidthDp: Dp = 90.dp,
    val edgePaddingDp: Dp = 8.dp,
    val elevationDp: Dp = 6.dp,
    val enableIconBlur: Boolean = false
) {
    companion object {
        @Composable
        fun defaultStyle(enableIconBlur: Boolean = false) = BarStyle(
            barContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            selectedTabContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            unselectedTabContainerColor = Color.Transparent,
            selectedTabContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            unselectedTabContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            enableIconBlur = enableIconBlur
        )
    }
}

@Composable
fun ExpressiveFloatingNavBar(
    items: List<NavTabItem>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    barStyle: BarStyle = BarStyle.defaultStyle(),
    showFab: Boolean = true,
    fabStyle: FabStyle? = null,
    onFabClick: (() -> Unit)? = null
) {
    require(items.size >= 2) { "ExpressiveFloatingNavBar requires at least 2 items." }

    val fabProgress by animateFloatAsState(
        targetValue = if (showFab && fabStyle != null) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "FabVisibilityProgress"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ScrollableFloatingPill(
            items = items,
            selectedIndex = selectedIndex,
            onTabSelected = onTabSelected,
            style = barStyle,
            modifier = Modifier.weight(1f, fill = false)
        )

        if (fabStyle != null && fabProgress > 0.005f) {
            val clampedLayoutProgress = fabProgress.coerceIn(0f, 1f)
            val fabSpacingDp = 12.dp

            Layout(
                content = {
                    Box(
                        modifier = Modifier.graphicsLayer {
                            alpha = clampedLayoutProgress
                            scaleX = 0.4f + (0.6f * fabProgress)
                            scaleY = 0.4f + (0.6f * fabProgress)
                            transformOrigin = TransformOrigin(0.5f, 0.5f)
                        }
                    ) {
                        CompanionFab(
                            style = fabStyle,
                            onClick = onFabClick,
                            enabled = showFab
                        )
                    }
                }
            ) { measurables, constraints ->
                val spacingPx = fabSpacingDp.roundToPx()
                val placeable = measurables.first().measure(constraints.copy(minWidth = 0, minHeight = 0))
                val fullWidth = placeable.width + spacingPx
                val currentWidth = (fullWidth * clampedLayoutProgress).roundToInt()

                layout(currentWidth, placeable.height) {
                    val xOffset = currentWidth - placeable.width
                    placeable.placeRelative(x = xOffset, y = 0)
                }
            }
        }
    }
}

@Composable
private fun CompanionFab(
    style: FabStyle,
    onClick: (() -> Unit)?,
    enabled: Boolean
) {
    val haptic = LocalHapticFeedback.current

    Surface(
        modifier = Modifier.size(style.sizeDp),
        shape = style.shape,
        color = style.containerColor,
        contentColor = style.contentColor,
        shadowElevation = style.elevationDp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(style.shape)
                .clickable(
                    enabled = enabled,
                    role = Role.Button
                ) {
                    haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
                    onClick?.invoke()
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = style.icon,
                contentDescription = style.contentDescription
            )
        }
    }
}

@Composable
private fun ScrollableFloatingPill(
    items: List<NavTabItem>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    style: BarStyle,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Surface(
        modifier = modifier.wrapContentSize(align = Alignment.Center),
        shape = style.shape,
        color = style.barContainerColor,
        shadowElevation = style.elevationDp
    ) {
        ScrollableTabsLayout(
            items = items,
            selectedTabIndex = selectedIndex,
            onTabSelected = onTabSelected,
            scrollState = scrollState,
            style = style
        )
    }
}

@Composable
private fun ScrollableTabsLayout(
    items: List<NavTabItem>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    scrollState: ScrollState,
    style: BarStyle
) {
    val coroutineScope = rememberCoroutineScope()
    val tabsScrollState = remember(scrollState, coroutineScope) {
        TabsScrollState(scrollState = scrollState, coroutineScope = coroutineScope)
    }

    Layout(
        content = {
            items.forEachIndexed { index, item ->
                ExpressiveToolbarTab(
                    leadingIcon = item.icon,
                    label = item.label,
                    contentDescription = item.contentDescription,
                    selected = index == selectedTabIndex,
                    onClick = { onTabSelected(index) },
                    style = style
                )
            }
        },
        modifier = Modifier
            .widthIn(max = style.maxWidthDp)
            .wrapContentSize(align = Alignment.Center)
            .padding(vertical = 8.dp)
            .horizontalScroll(scrollState)
            .selectableGroup()
            .clipToBounds()
    ) { measurables, constraints ->
        val edgePaddingPx = style.edgePaddingDp.roundToPx()
        val minTabWidthPx = style.minTabWidthDp.roundToPx()

        val layoutHeightPx = measurables.maxIntrinsicHeight()
        val tabConstraints = constraints.copy(
            minWidth = minTabWidthPx,
            minHeight = layoutHeightPx,
            maxHeight = layoutHeightPx
        )

        val tabPlaceables = measurables.fastMap { it.measure(tabConstraints) }

        var accumulatedWidthPx = edgePaddingPx
        val tabPositions = mutableListOf<TabPosition>()

        tabPlaceables.fastForEach { placeable ->
            val tabWidthPx = maxOf(minTabWidthPx, placeable.width)
            tabPositions.add(TabPosition(leftPx = accumulatedWidthPx, widthPx = tabWidthPx))
            accumulatedWidthPx += tabWidthPx
        }

        val totalLayoutWidthPx = accumulatedWidthPx + edgePaddingPx

        layout(totalLayoutWidthPx, layoutHeightPx) {
            tabPlaceables.fastForEachIndexed { index, placeable ->
                placeable.placeRelative(x = tabPositions[index].leftPx, y = 0)
            }

            tabsScrollState.scrollToSelectedTab(
                tabPositions = tabPositions,
                selectedTab = selectedTabIndex,
                totalWidthPx = totalLayoutWidthPx
            )
        }
    }
}

private data class TabPosition(val leftPx: Int, val widthPx: Int)

private class TabsScrollState(
    private val scrollState: ScrollState,
    private val coroutineScope: CoroutineScope
) {
    private var selectedTab: Int? = null

    fun scrollToSelectedTab(tabPositions: List<TabPosition>, selectedTab: Int, totalWidthPx: Int) {
        if (this.selectedTab != selectedTab) {
            this.selectedTab = selectedTab
            tabPositions.getOrNull(selectedTab)?.let { selectedTabPosition ->
                val targetScrollOffset = offsetRequiredToCenterTheTab(
                    tabPosition = selectedTabPosition,
                    totalToolbarWidth = totalWidthPx,
                    visibleRightOffset = scrollState.maxValue
                )

                if (scrollState.value != targetScrollOffset) {
                    coroutineScope.launch {
                        scrollState.animateScrollTo(
                            targetScrollOffset,
                            spring(Spring.DampingRatioNoBouncy, Spring.StiffnessMediumLow)
                        )
                    }
                }
            }
        }
    }

    private fun offsetRequiredToCenterTheTab(
        tabPosition: TabPosition,
        totalToolbarWidth: Int,
        visibleRightOffset: Int
    ): Int {
        val visibleToolbarWidth = totalToolbarWidth - visibleRightOffset
        val currentTabStart = tabPosition.leftPx
        val visibleToolbarCenter = visibleToolbarWidth / 2
        val tabHalfWidth = tabPosition.widthPx / 2
        val centeredTabOffset = currentTabStart - (visibleToolbarCenter - tabHalfWidth)
        return centeredTabOffset.coerceIn(0, visibleRightOffset.coerceAtLeast(0))
    }
}

private fun List<Measurable>.maxIntrinsicHeight() =
    fastFold(initial = 0) { curr, measurable ->
        maxOf(curr, measurable.maxIntrinsicHeight(Constraints.Infinity))
    }

@Composable
private fun ExpressiveToolbarTab(
    leadingIcon: ImageVector,
    label: String,
    contentDescription: String,
    selected: Boolean,
    onClick: () -> Unit,
    style: BarStyle
) {
    val haptic = LocalHapticFeedback.current

    val animatedBg by animateColorAsState(
        targetValue = if (selected) style.selectedTabContainerColor else style.unselectedTabContainerColor,
        animationSpec = spring(Spring.DampingRatioNoBouncy, Spring.StiffnessMedium),
        label = "TabBgColor"
    )

    val animatedContentColor by animateColorAsState(
        targetValue = if (selected) style.selectedTabContentColor else style.unselectedTabContentColor,
        animationSpec = spring(Spring.DampingRatioNoBouncy, Spring.StiffnessMedium),
        label = "TabContentColor"
    )

    val iconProgress by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "IconMorphProgress"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(color = animatedBg)
            .selectable(
                selected = selected,
                role = Role.Tab,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
                    onClick()
                }
            )
            .minimumInteractiveComponentSize()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        val clampedProgress = iconProgress.coerceIn(0f, 1f)
        if (clampedProgress > 0.005f) {
            val blurRadiusDp = ((1f - clampedProgress) * 10f).dp
            val blurModifier = if (
                style.enableIconBlur &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                blurRadiusDp > 0.2.dp
            ) {
                Modifier.blur(
                    radius = blurRadiusDp,
                    edgeTreatment = BlurredEdgeTreatment.Unbounded
                )
            } else {
                Modifier
            }

            Layout(
                content = {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .then(blurModifier)
                            .graphicsLayer {
                                alpha = clampedProgress
                                scaleX = 0.2f + (0.8f * iconProgress)
                                scaleY = 0.2f + (0.8f * iconProgress)
                                transformOrigin = TransformOrigin(0f, 0.5f)
                            },
                        tint = animatedContentColor
                    )
                }
            ) { measurables, constraints ->
                val placeable = measurables.first().measure(constraints)
                val currentWidth = (placeable.width * clampedProgress).roundToInt()
                layout(currentWidth, placeable.height) {
                    placeable.placeRelative(
                        x = 0,
                        y = 0
                    )
                }
            }
        }

        Text(
            modifier = Modifier.semantics { this.contentDescription = contentDescription },
            text = label,
            style = if (selected) MaterialTheme.typography.labelLarge else MaterialTheme.typography.bodyMedium,
            color = animatedContentColor
        )
    }
}