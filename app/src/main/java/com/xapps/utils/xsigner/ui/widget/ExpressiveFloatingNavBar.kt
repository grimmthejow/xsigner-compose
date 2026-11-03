package com.xapps.utils.xsigner.ui.widget

import android.os.Build
import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.graphics.ExperimentalAnimationGraphicsApi
import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.util.fastMap
import androidx.compose.ui.zIndex
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

sealed interface NavTabIcon {
    data class Vector(val icon: ImageVector) : NavTabIcon

    data class StatefulVector(
        val selectedIcon: ImageVector,
        val unselectedIcon: ImageVector,
    ) : NavTabIcon

    data class StatefulDrawable(
        @DrawableRes val selectedResId: Int,
        @DrawableRes val unselectedResId: Int,
    ) : NavTabIcon

    data class AnimatedVector(
        @DrawableRes val animatedVectorResId: Int,
        val popOnSelect: Boolean = false,
    ) : NavTabIcon
}

data class NavTabItem(
    val tabIcon: NavTabIcon,
    val label: String,
    val contentDescription: String = label,
) {
    constructor(
        icon: ImageVector,
        label: String,
        contentDescription: String = label,
    ) : this(NavTabIcon.Vector(icon), label, contentDescription)

    constructor(
        selectedIcon: ImageVector,
        unselectedIcon: ImageVector,
        label: String,
        contentDescription: String = label,
    ) : this(NavTabIcon.StatefulVector(selectedIcon, unselectedIcon), label, contentDescription)

    constructor(
        @DrawableRes animatedVectorResId: Int,
        label: String,
        contentDescription: String = label,
        popOnSelect: Boolean = false,
    ) : this(NavTabIcon.AnimatedVector(animatedVectorResId, popOnSelect), label, contentDescription)
}

data class FabStyle(
    val icon: ImageVector,
    val contentDescription: String,
    val containerColor: Color,
    val contentColor: Color,
    val shape: Shape = CircleShape,
    val sizeDp: Dp = 56.dp,
    val elevationDp: Dp = 6.dp,
) {
    companion object {
        @Composable
        fun defaultStyle(icon: ImageVector, contentDescription: String) = FabStyle(
            icon = icon,
            contentDescription = contentDescription,
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface,
        )

        @Composable
        fun secondaryStyle(icon: ImageVector, contentDescription: String) = FabStyle(
            icon = icon,
            contentDescription = contentDescription,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            shape = CircleShape,
            sizeDp = 56.dp,
        )

        @Composable
        fun primaryStyle(icon: ImageVector, contentDescription: String) = FabStyle(
            icon = icon,
            contentDescription = contentDescription,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = RoundedCornerShape(16.dp),
            sizeDp = 64.dp,
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
    val maxWidthDp: Dp = 360.dp,
    val tabHeightDp: Dp = 48.dp,
    val unselectedTabSizeDp: Dp = 48.dp,
    val edgePaddingDp: Dp = 8.dp,
    val tabSpacingDp: Dp = 6.dp,
    val elevationDp: Dp = 6.dp,
    val enableBlur: Boolean = false,
) {
    companion object {
        @Composable
        fun defaultStyle(enableBlur: Boolean = false): BarStyle {
            val selectedBg = MaterialTheme.colorScheme.secondaryContainer
            return BarStyle(
                barContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                selectedTabContainerColor = selectedBg,
                unselectedTabContainerColor = selectedBg.copy(alpha = 0f),
                selectedTabContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                unselectedTabContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                enableBlur = enableBlur,
            )
        }
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
    onFabClick: (() -> Unit)? = null,
) {
    require(items.size >= 2) { "ExpressiveFloatingNavBar requires at least 2 items." }

    val fabProgress by animateFloatAsState(
        targetValue = if (showFab && fabStyle != null) 1f else 0f,
        animationSpec = spring(
            dampingRatio = 0.65f,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "FabVisibilityProgress",
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ScrollableFloatingPill(
            items = items,
            selectedIndex = selectedIndex,
            onTabSelected = onTabSelected,
            style = barStyle,
            modifier = Modifier
                .zIndex(1f)
                .weight(1f, fill = false),
        )

        if (fabStyle != null && (fabProgress > 0.005f)) {
            val boundedFabProgress = fabProgress.coerceAtLeast(0f)
            val clampedLayoutProgress = fabProgress.coerceIn(0f, 1f)
            val fabSpacingDp = 12.dp

            val fabBlurRadius = ((1f - clampedLayoutProgress) * 10f).dp
            val fabBlurModifier = if (
                barStyle.enableBlur &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                fabBlurRadius > 0.2.dp
            ) {
                Modifier.blur(
                    radius = fabBlurRadius,
                    edgeTreatment = BlurredEdgeTreatment.Unbounded,
                )
            } else {
                Modifier
            }

            Layout(
                modifier = Modifier.zIndex(0f),
                content = {
                    Box(
                        modifier = Modifier
                            .then(fabBlurModifier)
                            .graphicsLayer {
                                scaleX = 0.35f + (0.65f * fabProgress)
                                scaleY = 0.35f + (0.65f * fabProgress)
                                transformOrigin = TransformOrigin(0f, 0.5f)
                            },
                    ) {
                        CompanionFab(
                            style = fabStyle,
                            progress = clampedLayoutProgress,
                            onClick = onFabClick,
                            enabled = showFab,
                        )
                    }
                },
            ) { measurables, constraints ->
                val spacingPx = fabSpacingDp.roundToPx()
                val placeable = measurables.first().measure(constraints.copy(minWidth = 0, minHeight = 0))
                val fullWidth = placeable.width + spacingPx
                val currentWidth = (fullWidth * boundedFabProgress).roundToInt().coerceAtLeast(0)

                layout(currentWidth, placeable.height) {
                    val xOffset = currentWidth - placeable.width -
                            ((1f - clampedLayoutProgress) * (placeable.width * 0.45f)).roundToInt()
                    placeable.placeRelative(x = xOffset, y = 0)
                }
            }
        }
    }
}

@Composable
private fun CompanionFab(
    style: FabStyle,
    progress: Float,
    onClick: (() -> Unit)?,
    enabled: Boolean,
) {
    val haptic = LocalHapticFeedback.current
    val animatedShadow = style.elevationDp * progress

    Surface(
        modifier = Modifier.size(style.sizeDp),
        shape = style.shape,
        color = style.containerColor.copy(alpha = style.containerColor.alpha * progress),
        contentColor = style.contentColor.copy(alpha = style.contentColor.alpha * progress),
        shadowElevation = animatedShadow,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(style.shape)
                .clickable(
                    enabled = enabled,
                    role = Role.Button,
                ) {
                    haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
                    onClick?.invoke()
                },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = style.icon,
                contentDescription = style.contentDescription,
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
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()

    Surface(
        modifier = modifier.wrapContentSize(align = Alignment.Center),
        shape = style.shape,
        color = style.barContainerColor,
        shadowElevation = style.elevationDp,
    ) {
        ScrollableTabsLayout(
            items = items,
            selectedTabIndex = selectedIndex,
            onTabSelected = onTabSelected,
            scrollState = scrollState,
            style = style,
        )
    }
}

@Composable
private fun ScrollableTabsLayout(
    items: List<NavTabItem>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    scrollState: ScrollState,
    style: BarStyle,
) {
    val coroutineScope = rememberCoroutineScope()
    val tabsScrollState = remember(scrollState, coroutineScope) {
        TabsScrollState(scrollState = scrollState, coroutineScope = coroutineScope)
    }

    Layout(
        content = {
            items.forEachIndexed { index, item ->
                ExpressiveToolbarTab(
                    tabIcon = item.tabIcon,
                    label = item.label,
                    contentDescription = item.contentDescription,
                    selected = index == selectedTabIndex,
                    onClick = { onTabSelected(index) },
                    style = style,
                )
            }
        },
        modifier = Modifier
            .widthIn(max = style.maxWidthDp)
            .wrapContentSize(align = Alignment.Center)
            .padding(vertical = 8.dp)
            .horizontalScroll(scrollState)
            .selectableGroup()
            .clipToBounds(),
    ) { measurables, constraints ->
        val edgePaddingPx = style.edgePaddingDp.roundToPx()
        val minTabWidthPx = style.unselectedTabSizeDp.roundToPx()
        val tabHeightPx = style.tabHeightDp.roundToPx()
        val tabSpacingPx = style.tabSpacingDp.roundToPx()

        val tabConstraints = constraints.copy(
            minWidth = minTabWidthPx,
            minHeight = tabHeightPx,
            maxHeight = tabHeightPx,
        )

        val tabPlaceables = measurables.fastMap { it.measure(tabConstraints) }

        var accumulatedWidthPx = edgePaddingPx
        val tabPositions = mutableListOf<TabPosition>()

        tabPlaceables.fastForEachIndexed { index, placeable ->
            val tabWidthPx = maxOf(minTabWidthPx, placeable.width)
            if (index > 0) {
                accumulatedWidthPx += tabSpacingPx
            }
            tabPositions.add(TabPosition(leftPx = accumulatedWidthPx, widthPx = tabWidthPx))
            accumulatedWidthPx += tabWidthPx
        }

        val totalLayoutWidthPx = accumulatedWidthPx + edgePaddingPx

        layout(totalLayoutWidthPx, tabHeightPx) {
            tabPlaceables.fastForEachIndexed { index, placeable ->
                placeable.placeRelative(x = tabPositions[index].leftPx, y = 0)
            }

            tabsScrollState.scrollToSelectedTab(
                tabPositions = tabPositions,
                selectedTab = selectedTabIndex,
                totalWidthPx = totalLayoutWidthPx,
            )
        }
    }
}

private data class TabPosition(val leftPx: Int, val widthPx: Int)

private class TabsScrollState(
    private val scrollState: ScrollState,
    private val coroutineScope: CoroutineScope,
) {
    private var selectedTab: Int? = null

    fun scrollToSelectedTab(tabPositions: List<TabPosition>, selectedTab: Int, totalWidthPx: Int) {
        if (this.selectedTab != selectedTab) {
            this.selectedTab = selectedTab
            tabPositions.getOrNull(selectedTab)?.let { selectedTabPosition ->
                val targetScrollOffset = offsetRequiredToCenterTheTab(
                    tabPosition = selectedTabPosition,
                    totalToolbarWidth = totalWidthPx,
                    visibleRightOffset = scrollState.maxValue,
                )

                if (scrollState.value != targetScrollOffset) {
                    coroutineScope.launch {
                        scrollState.animateScrollTo(
                            targetScrollOffset,
                            spring(0.65f, Spring.StiffnessMediumLow),
                        )
                    }
                }
            }
        }
    }

    private fun offsetRequiredToCenterTheTab(
        tabPosition: TabPosition,
        totalToolbarWidth: Int,
        visibleRightOffset: Int,
    ): Int {
        val visibleToolbarWidth = totalToolbarWidth - visibleRightOffset
        val currentTabStart = tabPosition.leftPx
        val visibleToolbarCenter = visibleToolbarWidth / 2
        val tabHalfWidth = tabPosition.widthPx / 2
        val centeredTabOffset = currentTabStart - (visibleToolbarCenter - tabHalfWidth)
        return centeredTabOffset.coerceIn(0, visibleRightOffset.coerceAtLeast(0))
    }
}

@OptIn(ExperimentalAnimationGraphicsApi::class)
@Composable
private fun ExpressiveToolbarTab(
    tabIcon: NavTabIcon,
    label: String,
    contentDescription: String,
    selected: Boolean,
    onClick: () -> Unit,
    style: BarStyle,
) {
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = 0.65f,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "TabPressScale",
    )

    val targetCornerRadius = when {
        selected && isPressed -> 12.dp
        selected && !isPressed -> style.tabHeightDp / 2
        !selected && isPressed -> 14.dp
        else -> style.unselectedTabSizeDp / 2
    }
    val cornerRadius by animateDpAsState(
        targetValue = targetCornerRadius,
        animationSpec = spring(
            dampingRatio = 0.65f,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "TabShapeMorph",
    )
    val morphingShape = RoundedCornerShape(cornerRadius)

    val animatedBg by animateColorAsState(
        targetValue = if (selected) style.selectedTabContainerColor else style.unselectedTabContainerColor,
        animationSpec = spring(0.65f, Spring.StiffnessMedium),
        label = "TabBgColor",
    )

    val animatedContentColor by animateColorAsState(
        targetValue = if (selected) style.selectedTabContentColor else style.unselectedTabContentColor,
        animationSpec = spring(0.65f, Spring.StiffnessMedium),
        label = "TabContentColor",
    )

    val expansionProgress by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = spring(
            dampingRatio = 0.6f,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "TabExpansionProgress",
    )

    val titleAppearProgress by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = spring(
            dampingRatio = 0.6f,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "TitleSharedAxisProgress",
    )

    val shouldPopIcon = when (tabIcon) {
        is NavTabIcon.AnimatedVector -> tabIcon.popOnSelect
        else -> true
    }

    val iconPopScale = remember { Animatable(1f) }
    LaunchedEffect(selected, shouldPopIcon) {
        if (selected && shouldPopIcon) {
            iconPopScale.snapTo(1f)
            iconPopScale.animateTo(
                targetValue = 1.2f,
                animationSpec = tween(durationMillis = 100, easing = FastOutSlowInEasing),
            )
            iconPopScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = 0.5f,
                    stiffness = Spring.StiffnessMedium,
                ),
            )
        } else {
            iconPopScale.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 100),
            )
        }
    }

    val iconPainter = when (tabIcon) {
        is NavTabIcon.Vector -> rememberVectorPainter(tabIcon.icon)
        is NavTabIcon.StatefulVector -> rememberVectorPainter(
            if (selected) tabIcon.selectedIcon else tabIcon.unselectedIcon,
        )
        is NavTabIcon.StatefulDrawable -> painterResource(
            id = if (selected) tabIcon.selectedResId else tabIcon.unselectedResId,
        )
        is NavTabIcon.AnimatedVector -> {
            val avd = AnimatedImageVector.animatedVectorResource(id = tabIcon.animatedVectorResId)
            rememberAnimatedVectorPainter(animatedImageVector = avd, atEnd = selected)
        }
    }

    val horizontalPadding by animateDpAsState(
        targetValue = if (selected) 16.dp else 12.dp,
        animationSpec = spring(0.65f, Spring.StiffnessMediumLow),
        label = "TabHorizontalPadding",
    )

    Row(
        modifier = Modifier
            .height(style.tabHeightDp)
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .clip(morphingShape)
            .background(color = animatedBg, shape = morphingShape)
            .selectable(
                selected = selected,
                interactionSource = interactionSource,
                indication = ripple(
                    bounded = true,
                    color = if (selected) style.selectedTabContentColor else style.unselectedTabContentColor,
                ),
                role = Role.Tab,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
                    onClick()
                },
            )
            .padding(horizontal = horizontalPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(
            painter = iconPainter,
            contentDescription = if (selected) null else contentDescription,
            modifier = Modifier
                .size(22.dp)
                .graphicsLayer {
                    scaleX = iconPopScale.value
                    scaleY = iconPopScale.value
                },
            tint = animatedContentColor,
        )

        val boundedExpansion = expansionProgress.coerceAtLeast(0f)
        if (boundedExpansion > 0.005f || titleAppearProgress > 0.005f) {
            val blurRadiusDp = ((1f - titleAppearProgress.coerceIn(0f, 1f)) * 10f).dp
            val titleBlurModifier = if (
                style.enableBlur &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                blurRadiusDp > 0.2.dp
            ) {
                Modifier.blur(
                    radius = blurRadiusDp,
                    edgeTreatment = BlurredEdgeTreatment.Unbounded,
                )
            } else {
                Modifier
            }

            val sharedAxisSlidePx = with(density) { 12.dp.toPx() }

            Layout(
                content = {
                    Text(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .then(titleBlurModifier)
                            .graphicsLayer {
                                alpha = titleAppearProgress.coerceIn(0f, 1f)
                                translationX = (1f - titleAppearProgress) * sharedAxisSlidePx
                                scaleX = 0.92f + (0.08f * titleAppearProgress)
                                scaleY = 0.92f + (0.08f * titleAppearProgress)
                                transformOrigin = TransformOrigin(0f, 0.5f)
                            }
                            .semantics { this.contentDescription = contentDescription },
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                        color = animatedContentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Clip,
                    )
                },
            ) { measurables, constraints ->
                val placeable = measurables.first().measure(constraints)
                val currentWidth = (placeable.width * boundedExpansion).roundToInt().coerceAtLeast(0)
                layout(currentWidth, placeable.height) {
                    placeable.placeRelative(x = 0, y = 0)
                }
            }
        }
    }
}
