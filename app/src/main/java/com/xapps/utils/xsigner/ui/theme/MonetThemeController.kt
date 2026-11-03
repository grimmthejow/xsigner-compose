package com.xapps.utils.xsigner.ui.theme

import android.app.Activity
import android.content.Context
import com.xapps.utils.xsigner.pref.LocalRuntimePrefs
import com.xapps.utils.xsigner.pref.RuntimePrefs
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.PixelCopy
import android.view.View
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sign
import kotlin.math.sin

@Suppress("unused")
@Stable
class MonetThemeController(
    val runtimePrefs: RuntimePrefs,
    initialSystemDark: Boolean = false,
    private val appLightScheme: ColorScheme,
    private val appDarkScheme: ColorScheme,
    private val context: Context,
    private val scope: CoroutineScope,
    internal val compositeGraphicsLayer: GraphicsLayer
) {
    enum class RevealShape {
        CIRCLE,
        SUPERELLIPSE_SQUIRCLE,
        COOKIE_12,
        COOKIE_9,
        STAR_8,
        CLOVER_4,
        SCALLOP_6
    }

    data class SwatchPreview(
        val topLeftPrimary: Color,
        val topRightSecondaryContainer: Color,
        val bottomLeftTertiaryContainer: Color,
        val bottomRightPrimaryContainer: Color
    )

    val seedColor: Color? get() = runtimePrefs.seedColor
    val themeMode: Int get() = runtimePrefs.themeMode
    val isDynamicColorEnabled: Boolean get() = runtimePrefs.isDynamicColorEnabled

    var systemIsDark by mutableStateOf(initialSystemDark)
        internal set

    val isDark: Boolean
        get() = when (themeMode) {
            1 -> false
            2 -> true
            else -> systemIsDark
        }

    internal fun evaluateDarknessChange(oldDark: Boolean) {
        val newDark = isDark
        if (oldDark != newDark) {
            colorScheme = buildScheme(seedColor, newDark)
        }
    }
    
    internal fun updateSystemTheme(isSystemDark: Boolean) {
        if (systemIsDark == isSystemDark) return
        val oldDark = isDark
        systemIsDark = isSystemDark
        evaluateDarknessChange(oldDark)
    }

    var isRevealing by mutableStateOf(false)
        private set

    var previousBitmap by mutableStateOf<ImageBitmap?>(null)
        private set

    var activeRevealShape by mutableStateOf(RevealShape.CIRCLE)
        private set

    var revealOrigin by mutableStateOf(Offset.Zero)
        private set

    internal var hostRootOffset by mutableStateOf(Offset.Zero)
    internal var hostSize by mutableStateOf(IntSize.Zero)
    internal var isLayerRecorded by mutableStateOf(false)
    internal var hostView: View? = null

    val progress = Animatable(1f)

    private var activeRevealJob: Job? = null

    val isBusy: Boolean
        get() = isRevealing || activeRevealJob?.isActive == true

    fun setDynamicColorEnabled(enabled: Boolean) {
        if (runtimePrefs.isDynamicColorEnabled == enabled) return
        runtimePrefs.isDynamicColorEnabled = enabled
        colorScheme = buildScheme(seedColor, isDark)
    }

    private fun buildScheme(seed: Color?, dark: Boolean): ColorScheme {
        if (!isDynamicColorEnabled) {
            return if (dark) appDarkScheme else appLightScheme
        }
        if (seed != null) {
            return generateMonetScheme(seed, dark)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        return if (dark) appDarkScheme else appLightScheme
    }

    var colorScheme by mutableStateOf(buildScheme(seedColor, isDark))
        private set

    fun setSeedColor(newSeed: Color?) {
        if (newSeed == seedColor) return
        runtimePrefs.seedColor = newSeed
        colorScheme = buildScheme(newSeed, isDark)
    }

    fun setThemeMode(newMode: Int) {
        if (newMode == themeMode) return
        val oldDark = isDark
        runtimePrefs.themeMode = newMode
        evaluateDarknessChange(oldDark)
    }

    fun selectSeedColor(
        newSeed: Color?,
        tapOriginInRoot: Offset,
        shape: RevealShape? = null,
        durationMillis: Int = 750
    ) {
        if (isBusy || newSeed == seedColor) return
        animateThemeMutation(
            tapOriginInRoot = tapOriginInRoot,
            shape = shape,
            durationMillis = durationMillis
        ) {
            setSeedColor(newSeed)
        }
    }

    fun selectThemeMode(
        newMode: Int,
        tapOriginInRoot: Offset = Offset.Zero,
        shape: RevealShape? = null,
        durationMillis: Int = 750
    ) {
        if (newMode == themeMode) return
        setThemeMode(newMode)
    }

    fun animateThemeMutation(
        tapOriginInRoot: Offset,
        shape: RevealShape? = null,
        durationMillis: Int = 750,
        mutation: () -> Unit
    ) {
        if (isBusy) return

        activeRevealJob = scope.launch {
            isRevealing = true
            revealOrigin = tapOriginInRoot - hostRootOffset
            activeRevealShape = shape ?: RevealShape.entries.random()

            try {
                if (!isLayerRecorded) {
                    withFrameNanos { }
                }

                val capturedSnapshot = captureHardwareBitmapWithShadows()
                    ?: runCatching {
                        if (isLayerRecorded) compositeGraphicsLayer.toImageBitmap() else null
                    }.getOrNull()

                if (capturedSnapshot == null) {
                    mutation()
                    return@launch
                }

                previousBitmap = capturedSnapshot
                progress.snapTo(0f)
                mutation()

                progress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = durationMillis,
                        easing = FastOutSlowInEasing
                    )
                )
            } finally {
                isRevealing = false
                previousBitmap = null
            }
        }
    }

    private suspend fun captureHardwareBitmapWithShadows(): ImageBitmap? {
        val view = hostView ?: return null
        val width = hostSize.width
        val height = hostSize.height
        if (width <= 0 || height <= 0) return null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val window = view.context.findActivity()?.window
            if (window != null) {
                val locationInWindow = IntArray(2)
                view.getLocationInWindow(locationInWindow)
                val left = locationInWindow[0] + hostRootOffset.x.roundToInt()
                val top = locationInWindow[1] + hostRootOffset.y.roundToInt()
                val srcRect = android.graphics.Rect(left, top, left + width, top + height)
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

                val copied = suspendCancellableCoroutine<Boolean> { cont ->
                    try {
                        PixelCopy.request(
                            window,
                            srcRect,
                            bitmap,
                            { copyResult ->
                                if (cont.isActive) {
                                    cont.resume(copyResult == PixelCopy.SUCCESS)
                                }
                            },
                            Handler(Looper.getMainLooper())
                        )
                    } catch (e: Exception) {
                        if (cont.isActive) {
                            cont.resume(false)
                        }
                    }
                }
                if (copied) {
                    return bitmap.asImageBitmap()
                }
            }
        }
        return null
    }

    internal fun buildClipShape(): Shape {
        val localTapOrigin = revealOrigin
        val currentProgress = progress.value
        val activeShape = activeRevealShape

        return object : Shape {
            override fun createOutline(
                size: Size,
                layoutDirection: LayoutDirection,
                density: Density
            ): Outline {
                if (currentProgress <= 0f || size.isEmpty()) {
                    return Outline.Generic(Path())
                }

                val clampedOrigin = Offset(
                    x = localTapOrigin.x.coerceIn(0f, size.width),
                    y = localTapOrigin.y.coerceIn(0f, size.height)
                )

                val furthestCornerDistance = hypot(
                    maxOf(clampedOrigin.x, size.width - clampedOrigin.x),
                    maxOf(clampedOrigin.y, size.height - clampedOrigin.y)
                )

                val rotationRad = (1f - currentProgress) * 0.38f

                val path = when (activeShape) {
                    RevealShape.CIRCLE -> {
                        val radius = furthestCornerDistance * currentProgress * 1.02f
                        Path().apply {
                            addOval(Rect(center = clampedOrigin, radius = radius))
                        }
                    }
                    RevealShape.SUPERELLIPSE_SQUIRCLE -> {
                        val radius = furthestCornerDistance * currentProgress * 1.22f
                        buildSuperellipsePath(
                            center = clampedOrigin,
                            radius = radius,
                            exponent = 3.6f,
                            rotationOffset = rotationRad * 0.4f
                        )
                    }
                    RevealShape.COOKIE_12 -> {
                        val outerRadius = computeGuaranteedOuterRadius(
                            maxDistance = furthestCornerDistance,
                            progress = currentProgress,
                            points = 12,
                            innerRatio = 0.86f
                        )
                        buildSmoothPolygonPath(
                            center = clampedOrigin,
                            outerRadius = outerRadius,
                            points = 12,
                            innerRadiusRatio = 0.86f,
                            rotationOffset = rotationRad
                        )
                    }
                    RevealShape.COOKIE_9 -> {
                        val outerRadius = computeGuaranteedOuterRadius(
                            maxDistance = furthestCornerDistance,
                            progress = currentProgress,
                            points = 9,
                            innerRatio = 0.82f
                        )
                        buildSmoothPolygonPath(
                            center = clampedOrigin,
                            outerRadius = outerRadius,
                            points = 9,
                            innerRadiusRatio = 0.82f,
                            rotationOffset = rotationRad
                        )
                    }
                    RevealShape.STAR_8 -> {
                        val outerRadius = computeGuaranteedOuterRadius(
                            maxDistance = furthestCornerDistance,
                            progress = currentProgress,
                            points = 8,
                            innerRatio = 0.72f
                        )
                        buildSmoothPolygonPath(
                            center = clampedOrigin,
                            outerRadius = outerRadius,
                            points = 8,
                            innerRadiusRatio = 0.72f,
                            rotationOffset = rotationRad
                        )
                    }
                    RevealShape.CLOVER_4 -> {
                        val outerRadius = computeGuaranteedOuterRadius(
                            maxDistance = furthestCornerDistance,
                            progress = currentProgress,
                            points = 4,
                            innerRatio = 0.68f
                        )
                        buildSmoothPolygonPath(
                            center = clampedOrigin,
                            outerRadius = outerRadius,
                            points = 4,
                            innerRadiusRatio = 0.68f,
                            rotationOffset = rotationRad
                        )
                    }
                    RevealShape.SCALLOP_6 -> {
                        val outerRadius = computeGuaranteedOuterRadius(
                            maxDistance = furthestCornerDistance,
                            progress = currentProgress,
                            points = 6,
                            innerRatio = 0.78f
                        )
                        buildSmoothPolygonPath(
                            center = clampedOrigin,
                            outerRadius = outerRadius,
                            points = 6,
                            innerRadiusRatio = 0.78f,
                            rotationOffset = rotationRad
                        )
                    }
                }

                return Outline.Generic(path)
            }
        }
    }

    private fun computeGuaranteedOuterRadius(
        maxDistance: Float,
        progress: Float,
        points: Int,
        innerRatio: Float
    ): Float {
        val apothemDiscount = cos(PI / (points * 2.0)).toFloat().coerceAtLeast(0.7f)
        val minimumInscribedRatio = innerRatio * apothemDiscount
        val safetyMultiplier = (1f / minimumInscribedRatio) * 1.06f
        return maxDistance * progress * safetyMultiplier
    }

    private fun buildSuperellipsePath(
        center: Offset,
        radius: Float,
        exponent: Float,
        rotationOffset: Float,
        steps: Int = 72
    ): Path {
        val path = Path()
        val power = 2f / exponent
        val cosRot = cos(rotationOffset)
        val sinRot = sin(rotationOffset)

        for (i in 0 until steps) {
            val theta = (2.0 * PI * i / steps).toFloat()
            val cosT = cos(theta)
            val sinT = sin(theta)

            val rawX = radius * sign(cosT) * abs(cosT).pow(power)
            val rawY = radius * sign(sinT) * abs(sinT).pow(power)

            val rotatedX = center.x + (rawX * cosRot - rawY * sinRot)
            val rotatedY = center.y + (rawX * sinRot + rawY * cosRot)

            if (i == 0) {
                path.moveTo(rotatedX, rotatedY)
            } else {
                path.lineTo(rotatedX, rotatedY)
            }
        }

        path.close()
        return path
    }

    private fun buildSmoothPolygonPath(
        center: Offset,
        outerRadius: Float,
        points: Int,
        innerRadiusRatio: Float,
        rotationOffset: Float
    ): Path {
        val path = Path()
        val totalVertices = points * 2
        val step = (2.0 * PI / totalVertices).toFloat()
        val vertices = ArrayList<Offset>(totalVertices)

        for (i in 0 until totalVertices) {
            val angle = i * step - (PI / 2.0).toFloat() + rotationOffset
            val r = if (i % 2 == 0) outerRadius else outerRadius * innerRadiusRatio
            vertices.add(
                Offset(
                    x = center.x + r * cos(angle),
                    y = center.y + r * sin(angle)
                )
            )
        }

        val startMidX = (vertices[0].x + vertices[totalVertices - 1].x) / 2f
        val startMidY = (vertices[0].y + vertices[totalVertices - 1].y) / 2f
        path.moveTo(startMidX, startMidY)

        for (i in 0 until totalVertices) {
            val current = vertices[i]
            val next = vertices[(i + 1) % totalVertices]
            val midX = (current.x + next.x) / 2f
            val midY = (current.y + next.y) / 2f
            path.quadraticTo(current.x, current.y, midX, midY)
        }

        path.close()
        return path
    }

    companion object {
        private fun normalizeHue(hue: Float): Float {
            return ((hue % 360f) + 360f) % 360f
        }

        private fun hctToColor(hue: Float, chroma: Float, tone: Float): Color {
            val safeHue = normalizeHue(hue)
            val safeChroma = max(0f, chroma)
            val safeTone = tone.coerceIn(0f, 100f)
            return Color(ColorUtils.M3HCTToColor(safeHue, safeChroma, safeTone))
        }

        private fun computeTertiaryHue(primaryHue: Float): Float {
            val normalized = normalizeHue(primaryHue)
            return if (normalized in 20f..115f) {
                normalizeHue(normalized - 48f)
            } else {
                normalizeHue(normalized + 60f)
            }
        }

        fun generateSwatchPreview(seed: Color?, isDark: Boolean = true): SwatchPreview {
            if (seed == null) {
                return SwatchPreview(Color.Unspecified, Color.Unspecified, Color.Unspecified, Color.Unspecified)
            }
            
            val hct = FloatArray(3)
            ColorUtils.colorToM3HCT(seed.toArgb(), hct)
            val hue = hct[0]
            val seedChroma = hct[1]

            val isNeutral = seedChroma < 5f
            val pChroma = when {
                isNeutral -> 0f
                seedChroma < 24f -> seedChroma.coerceAtLeast(18f)
                else -> 40f
            }
            val sChroma = if (isNeutral) 0f else 16f
            val tChroma = if (isNeutral) 0f else 24f
            val tHue = computeTertiaryHue(hue)

            return if (isDark) {
                SwatchPreview(
                    topLeftPrimary = hctToColor(hue, pChroma, 76f),
                    topRightSecondaryContainer = hctToColor(hue, sChroma, 28f),
                    bottomLeftTertiaryContainer = hctToColor(tHue, tChroma, 88f),
                    bottomRightPrimaryContainer = hctToColor(hue, pChroma, 84f)
                )
            } else {
                SwatchPreview(
                    topLeftPrimary = hctToColor(hue, pChroma, 42f),
                    topRightSecondaryContainer = hctToColor(hue, sChroma, 86f),
                    bottomLeftTertiaryContainer = hctToColor(tHue, tChroma, 80f),
                    bottomRightPrimaryContainer = hctToColor(hue, pChroma, 90f)
                )
            }
        }

        fun generateMonetScheme(seed: Color, isDark: Boolean): ColorScheme {
            val hct = FloatArray(3)
            ColorUtils.colorToM3HCT(seed.toArgb(), hct)
            val hue = hct[0]
            val seedChroma = hct[1]

            val isNeutral = seedChroma < 5f

            val pChroma = when {
                isNeutral -> 0f
                seedChroma < 24f -> seedChroma.coerceAtLeast(18f)
                else -> 40f
            }
            val sChroma = if (isNeutral) 0f else 16f
            val tHue = computeTertiaryHue(hue)
            val tChroma = if (isNeutral) 0f else 24f
            val nChroma = if (isNeutral) 0f else 6f
            val nvChroma = if (isNeutral) 0f else 8f

            val errHue = 25f
            val errChroma = 84f

            return if (isDark) {
                darkColorScheme(
                    primary = hctToColor(hue, pChroma, 80f),
                    onPrimary = hctToColor(hue, pChroma, 20f),
                    primaryContainer = hctToColor(hue, pChroma, 30f),
                    onPrimaryContainer = hctToColor(hue, pChroma, 90f),
                    inversePrimary = hctToColor(hue, pChroma, 40f),

                    secondary = hctToColor(hue, sChroma, 80f),
                    onSecondary = hctToColor(hue, sChroma, 20f),
                    secondaryContainer = hctToColor(hue, sChroma, 30f),
                    onSecondaryContainer = hctToColor(hue, sChroma, 90f),

                    tertiary = hctToColor(tHue, tChroma, 80f),
                    onTertiary = hctToColor(tHue, tChroma, 20f),
                    tertiaryContainer = hctToColor(tHue, tChroma, 30f),
                    onTertiaryContainer = hctToColor(tHue, tChroma, 90f),

                    background = hctToColor(hue, nChroma, 6f),
                    onBackground = hctToColor(hue, nChroma, 90f),

                    surface = hctToColor(hue, nChroma, 6f),
                    onSurface = hctToColor(hue, nChroma, 90f),
                    surfaceVariant = hctToColor(hue, nvChroma, 30f),
                    onSurfaceVariant = hctToColor(hue, nvChroma, 80f),
                    surfaceTint = hctToColor(hue, pChroma, 80f),
                    inverseSurface = hctToColor(hue, nChroma, 90f),
                    inverseOnSurface = hctToColor(hue, nChroma, 20f),

                    error = hctToColor(errHue, errChroma, 80f),
                    onError = hctToColor(errHue, errChroma, 20f),
                    errorContainer = hctToColor(errHue, errChroma, 30f),
                    onErrorContainer = hctToColor(errHue, errChroma, 90f),

                    outline = hctToColor(hue, nvChroma, 60f),
                    outlineVariant = hctToColor(hue, nvChroma, 30f),
                    scrim = Color.Black,

                    surfaceBright = hctToColor(hue, nChroma, 24f),
                    surfaceDim = hctToColor(hue, nChroma, 6f),
                    surfaceContainerLowest = hctToColor(hue, nChroma, 4f),
                    surfaceContainerLow = hctToColor(hue, nChroma, 10f),
                    surfaceContainer = hctToColor(hue, nChroma, 12f),
                    surfaceContainerHigh = hctToColor(hue, nChroma, 17f),
                    surfaceContainerHighest = hctToColor(hue, nChroma, 22f)
                )
            } else {
                lightColorScheme(
                    primary = hctToColor(hue, pChroma, 40f),
                    onPrimary = hctToColor(hue, pChroma, 100f),
                    primaryContainer = hctToColor(hue, pChroma, 90f),
                    onPrimaryContainer = hctToColor(hue, pChroma, 10f),
                    inversePrimary = hctToColor(hue, pChroma, 80f),

                    secondary = hctToColor(hue, sChroma, 40f),
                    onSecondary = hctToColor(hue, sChroma, 100f),
                    secondaryContainer = hctToColor(hue, sChroma, 90f),
                    onSecondaryContainer = hctToColor(hue, sChroma, 10f),

                    tertiary = hctToColor(tHue, tChroma, 40f),
                    onTertiary = hctToColor(tHue, tChroma, 100f),
                    tertiaryContainer = hctToColor(tHue, tChroma, 90f),
                    onTertiaryContainer = hctToColor(tHue, tChroma, 10f),

                    background = hctToColor(hue, nChroma, 98f),
                    onBackground = hctToColor(hue, nChroma, 10f),

                    surface = hctToColor(hue, nChroma, 98f),
                    onSurface = hctToColor(hue, nChroma, 10f),
                    surfaceVariant = hctToColor(hue, nvChroma, 90f),
                    onSurfaceVariant = hctToColor(hue, nvChroma, 30f),
                    surfaceTint = hctToColor(hue, pChroma, 40f),
                    inverseSurface = hctToColor(hue, nChroma, 20f),
                    inverseOnSurface = hctToColor(hue, nChroma, 95f),

                    error = hctToColor(errHue, errChroma, 40f),
                    onError = hctToColor(errHue, errChroma, 100f),
                    errorContainer = hctToColor(errHue, errChroma, 90f),
                    onErrorContainer = hctToColor(errHue, errChroma, 10f),

                    outline = hctToColor(hue, nvChroma, 50f),
                    outlineVariant = hctToColor(hue, nvChroma, 80f),
                    scrim = Color.Black,

                    surfaceBright = hctToColor(hue, nChroma, 98f),
                    surfaceDim = hctToColor(hue, nChroma, 87f),
                    surfaceContainerLowest = hctToColor(hue, nChroma, 100f),
                    surfaceContainerLow = hctToColor(hue, nChroma, 96f),
                    surfaceContainer = hctToColor(hue, nChroma, 94f),
                    surfaceContainerHigh = hctToColor(hue, nChroma, 92f),
                    surfaceContainerHighest = hctToColor(hue, nChroma, 90f)
                )
            }
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

val LocalMonetThemeController = staticCompositionLocalOf<MonetThemeController> {
    error("MonetThemeController not initialized. Wrap your root UI in MonetThemeHost.")
}

@Composable
fun rememberMonetThemeController(
    runtimePrefs: RuntimePrefs = LocalRuntimePrefs.current,
    appLightScheme: ColorScheme = lightScheme,
    appDarkScheme: ColorScheme = darkScheme
): MonetThemeController {
    val context = LocalContext.current
    val defaultDark = isSystemInDarkTheme()
    
    val scope = rememberCoroutineScope()
    val compositeGraphicsLayer = rememberGraphicsLayer()
    return remember(scope, compositeGraphicsLayer, runtimePrefs) {
        MonetThemeController(
            runtimePrefs = runtimePrefs,
            initialSystemDark = defaultDark,
            appLightScheme = appLightScheme,
            appDarkScheme = appDarkScheme,
            context = context,
            scope = scope,
            compositeGraphicsLayer = compositeGraphicsLayer
        )
    }
}

@Suppress("unused")
@Composable
fun MonetThemeHost(
    modifier: Modifier = Modifier,
    controller: MonetThemeController = rememberMonetThemeController(),
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    controller.hostView = view
    
    val isSystemDark = isSystemInDarkTheme()
    SideEffect {
        controller.updateSystemTheme(isSystemDark)
    }

    val isCurrentlyDark = controller.isDark

    val window = remember(view) {
        (view.context as? Activity)?.window ?: view.context.findActivity()?.window
    }
    SideEffect {
        if (window != null) {
            val insetsController = WindowInsetsControllerCompat(window, window.decorView)
            insetsController.isAppearanceLightStatusBars = !isCurrentlyDark
            insetsController.isAppearanceLightNavigationBars = !isCurrentlyDark
        }
    }

    CompositionLocalProvider(LocalMonetThemeController provides controller) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .onGloballyPositioned { coordinates ->
                    controller.hostRootOffset = coordinates.positionInRoot()
                    controller.hostSize = coordinates.size
                }
                .then(
                    if (controller.isRevealing) {
                        Modifier.pointerInput(Unit) {
                            awaitEachGesture {
                                val down = awaitFirstDown(pass = PointerEventPass.Initial)
                                down.consume()
                            }
                        }
                    } else {
                        Modifier
                    }
                )
                .drawWithContent {
                    controller.compositeGraphicsLayer.record {
                        this@drawWithContent.drawContent()
                    }
                    controller.isLayerRecorded = true

                    if (controller.isRevealing) {
                        drawLayer(controller.compositeGraphicsLayer)

                        val oldBitmap = controller.previousBitmap
                        if (oldBitmap != null) {
                            val shape = controller.buildClipShape()
                            val outline = shape.createOutline(size, layoutDirection, this)
                            val shapePath = when (outline) {
                                is Outline.Generic -> outline.path
                                is Outline.Rectangle -> Path().apply { addRect(outline.rect) }
                                is Outline.Rounded -> Path().apply { addRoundRect(outline.roundRect) }
                            }
                            val clipPath = Path().apply {
                                addRect(Rect(0f, 0f, size.width, size.height))
                                addPath(shapePath)
                                fillType = PathFillType.EvenOdd
                            }
                            clipPath(clipPath) {
                                drawImage(
                                    image = oldBitmap,
                                    dstOffset = IntOffset.Zero,
                                    dstSize = IntSize(size.width.roundToInt(), size.height.roundToInt())
                                )
                            }
                        }
                    } else {
                        drawLayer(controller.compositeGraphicsLayer)
                    }
                }
        ) {
            MaterialTheme(
                colorScheme = controller.colorScheme.animateColors(),
                content = content
            )
        }
    }
}

@Composable
private fun ColorScheme.animateColors(
    animationSpec: androidx.compose.animation.core.AnimationSpec<Color> = tween(durationMillis = 500)
): ColorScheme {
    return copy(
        primary = animateColorAsState(primary, animationSpec, label = "primary").value,
        onPrimary = animateColorAsState(onPrimary, animationSpec, label = "onPrimary").value,
        primaryContainer = animateColorAsState(primaryContainer, animationSpec, label = "primaryContainer").value,
        onPrimaryContainer = animateColorAsState(onPrimaryContainer, animationSpec, label = "onPrimaryContainer").value,
        secondary = animateColorAsState(secondary, animationSpec, label = "secondary").value,
        onSecondary = animateColorAsState(onSecondary, animationSpec, label = "onSecondary").value,
        secondaryContainer = animateColorAsState(secondaryContainer, animationSpec, label = "secondaryContainer").value,
        onSecondaryContainer = animateColorAsState(onSecondaryContainer, animationSpec, label = "onSecondaryContainer").value,
        tertiary = animateColorAsState(tertiary, animationSpec, label = "tertiary").value,
        onTertiary = animateColorAsState(onTertiary, animationSpec, label = "onTertiary").value,
        tertiaryContainer = animateColorAsState(tertiaryContainer, animationSpec, label = "tertiaryContainer").value,
        onTertiaryContainer = animateColorAsState(onTertiaryContainer, animationSpec, label = "onTertiaryContainer").value,
        error = animateColorAsState(error, animationSpec, label = "error").value,
        onError = animateColorAsState(onError, animationSpec, label = "onError").value,
        errorContainer = animateColorAsState(errorContainer, animationSpec, label = "errorContainer").value,
        onErrorContainer = animateColorAsState(onErrorContainer, animationSpec, label = "onErrorContainer").value,
        background = animateColorAsState(background, animationSpec, label = "background").value,
        onBackground = animateColorAsState(onBackground, animationSpec, label = "onBackground").value,
        surface = animateColorAsState(surface, animationSpec, label = "surface").value,
        onSurface = animateColorAsState(onSurface, animationSpec, label = "onSurface").value,
        surfaceVariant = animateColorAsState(surfaceVariant, animationSpec, label = "surfaceVariant").value,
        onSurfaceVariant = animateColorAsState(onSurfaceVariant, animationSpec, label = "onSurfaceVariant").value,
        outline = animateColorAsState(outline, animationSpec, label = "outline").value,
        outlineVariant = animateColorAsState(outlineVariant, animationSpec, label = "outlineVariant").value,
        scrim = animateColorAsState(scrim, animationSpec, label = "scrim").value,
        inverseSurface = animateColorAsState(inverseSurface, animationSpec, label = "inverseSurface").value,
        inverseOnSurface = animateColorAsState(inverseOnSurface, animationSpec, label = "inverseOnSurface").value,
        inversePrimary = animateColorAsState(inversePrimary, animationSpec, label = "inversePrimary").value,
        surfaceDim = animateColorAsState(surfaceDim, animationSpec, label = "surfaceDim").value,
        surfaceBright = animateColorAsState(surfaceBright, animationSpec, label = "surfaceBright").value,
        surfaceContainerLowest = animateColorAsState(surfaceContainerLowest, animationSpec, label = "surfaceContainerLowest").value,
        surfaceContainerLow = animateColorAsState(surfaceContainerLow, animationSpec, label = "surfaceContainerLow").value,
        surfaceContainer = animateColorAsState(surfaceContainer, animationSpec, label = "surfaceContainer").value,
        surfaceContainerHigh = animateColorAsState(surfaceContainerHigh, animationSpec, label = "surfaceContainerHigh").value,
        surfaceContainerHighest = animateColorAsState(surfaceContainerHighest, animationSpec, label = "surfaceContainerHighest").value,
        surfaceTint = animateColorAsState(surfaceTint, animationSpec, label = "surfaceTint").value
    )
}

@Suppress("unused")
@Composable
fun Modifier.monetRevealTrigger(
    shape: MonetThemeController.RevealShape? = null,
    onMutateTheme: (MonetThemeController) -> Unit
): Modifier {
    val controller = LocalMonetThemeController.current
    var buttonPositionInRoot by remember { mutableStateOf(Offset.Zero) }

    return this
        .onGloballyPositioned { coordinates ->
            buttonPositionInRoot = coordinates.positionInRoot()
        }
        .pointerInput(shape, onMutateTheme) {
            detectTapGestures { localOffset ->
                if (!controller.isBusy) {
                    controller.animateThemeMutation(
                        tapOriginInRoot = buttonPositionInRoot + localOffset,
                        shape = shape
                    ) {
                        onMutateTheme(controller)
                    }
                }
            }
        }
}

@Suppress("unused")
@Composable
fun MonetColorSwatch(
    seedColor: Color?,
    modifier: Modifier = Modifier,
    size: Dp = 60.dp,
    cornerRadius: Dp = 20.dp,
    shapeOverride: MonetThemeController.RevealShape? = null
) {
    val controller = LocalMonetThemeController.current
    val context = LocalContext.current
    val isSelected = controller.seedColor == seedColor
    
    val targetPreview = remember(seedColor, controller.isDark, context) {
        if (seedColor == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val scheme = if (controller.isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
                MonetThemeController.SwatchPreview(
                    topLeftPrimary = scheme.primary,
                    topRightSecondaryContainer = scheme.secondaryContainer,
                    bottomLeftTertiaryContainer = scheme.tertiaryContainer,
                    bottomRightPrimaryContainer = scheme.primaryContainer
                )
            } else {
                MonetThemeController.generateSwatchPreview(Color(0xFF6750A4), controller.isDark)
            }
        } else {
            MonetThemeController.generateSwatchPreview(seedColor, controller.isDark)
        }
    }

    val colorAnimSpec = tween<Color>(durationMillis = 350)
    val animTopLeft by animateColorAsState(targetValue = targetPreview.topLeftPrimary, animationSpec = colorAnimSpec, label = "swatchTopLeft")
    val animTopRight by animateColorAsState(targetValue = targetPreview.topRightSecondaryContainer, animationSpec = colorAnimSpec, label = "swatchTopRight")
    val animBottomLeft by animateColorAsState(targetValue = targetPreview.bottomLeftTertiaryContainer, animationSpec = colorAnimSpec, label = "swatchBottomLeft")
    val animBottomRight by animateColorAsState(targetValue = targetPreview.bottomRightPrimaryContainer, animationSpec = colorAnimSpec, label = "swatchBottomRight")

    val borderWidth by animateDpAsState(
        targetValue = if (isSelected) 2.5.dp else 0.dp,
        animationSpec = tween(durationMillis = 150),
        label = "swatchBorderWidth"
    )

    val borderColor = MaterialTheme.colorScheme.primary
    val checkContainerColor = MaterialTheme.colorScheme.primaryContainer
    val checkColor = MaterialTheme.colorScheme.onPrimaryContainer

    var swatchPositionInRoot by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
            .then(
                if (borderWidth > 0.dp) {
                    Modifier.border(
                        width = borderWidth,
                        color = borderColor,
                        shape = RoundedCornerShape(cornerRadius)
                    )
                } else {
                    Modifier
                }
            )
            .onGloballyPositioned { coordinates ->
                swatchPositionInRoot = coordinates.positionInRoot()
            }
            .pointerInput(seedColor, shapeOverride) {
                detectTapGestures { localOffset ->
                    if (!controller.isBusy) {
                        controller.selectSeedColor(
                            newSeed = seedColor,
                            tapOriginInRoot = swatchPositionInRoot + localOffset,
                            shape = shapeOverride
                        )
                    }
                }
            }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(animTopLeft)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(animTopRight)
                )
            }
            Row(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(animBottomLeft)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(animBottomRight)
                )
            }
        }

        AnimatedVisibility(
            visible = isSelected,
            modifier = Modifier.align(Alignment.Center),
            enter = fadeIn(animationSpec = tween(200)) + scaleIn(animationSpec = tween(200), initialScale = 0.6f),
            exit = fadeOut(animationSpec = tween(150)) + scaleOut(animationSpec = tween(150), targetScale = 0.6f)
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(checkContainerColor),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(12.dp)) {
                    val path = Path().apply {
                        moveTo(this@Canvas.size.width * 0.18f, this@Canvas.size.height * 0.52f)
                        lineTo(this@Canvas.size.width * 0.42f, this@Canvas.size.height * 0.76f)
                        lineTo(this@Canvas.size.width * 0.84f, this@Canvas.size.height * 0.26f)
                    }
                    drawPath(
                        path = path,
                        color = checkColor,
                        style = Stroke(
                            width = 2.2.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
            }
        }
    }
}