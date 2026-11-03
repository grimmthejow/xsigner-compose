package com.xapps.utils.xsigner.ui.modifier

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class BounceOverscrollState(
    private val scope: CoroutineScope,
    private val orientation: Orientation = Orientation.Vertical
) {
    val overscrollOffset = Animatable(0f)

    companion object {
        const val MAX_DRAG = 800f
        const val FRICTION_BASE = 0.45f
        const val MAX_FLING_VELOCITY = 6000f
        const val SPRING_STIFFNESS = 200f
        const val SPRING_DAMPING = 1f
    }

    private var currentOffset = 0f
    private var animationJob: Job? = null

    private fun cancelAnimation() {
        animationJob?.cancel()
        animationJob = null
    }

    private fun snapOffset(newOffset: Float) {
        currentOffset = newOffset
        cancelAnimation()
        animationJob = scope.launch {
            overscrollOffset.snapTo(newOffset)
        }
    }

    private fun applyOverScroll(delta: Float) {
        val ratio = min(1f, abs(currentOffset / MAX_DRAG))
        val friction = FRICTION_BASE * (1f - ratio)
        snapOffset(currentOffset + delta * friction)
    }

    private fun triggerFlingSpring(rawVelocity: Float) {
        val velocity = rawVelocity.coerceIn(-MAX_FLING_VELOCITY, MAX_FLING_VELOCITY)
        val startOffset = currentOffset
        cancelAnimation()
        animationJob = scope.launch {
            if (overscrollOffset.value != startOffset) {
                overscrollOffset.snapTo(startOffset)
            }
            overscrollOffset.animateTo(
                targetValue = 0f,
                initialVelocity = velocity,
                animationSpec = spring(
                    dampingRatio = SPRING_DAMPING,
                    stiffness = SPRING_STIFFNESS
                )
            ) {
                currentOffset = value
            }
            currentOffset = 0f
        }
    }

    private fun springBackIfNeeded() {
        if (currentOffset != 0f && animationJob?.isActive != true) {
            triggerFlingSpring(0f)
        }
    }

    val connection = object : NestedScrollConnection {

        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            if (source != NestedScrollSource.UserInput) return Offset.Zero
            if (currentOffset == 0f) return Offset.Zero

            val delta = if (orientation == Orientation.Vertical) available.y else available.x
            if (delta == 0f) return Offset.Zero

            val scrollingTowardsZero =
                (currentOffset > 0f && delta < 0f) || (currentOffset < 0f && delta > 0f)

            return if (scrollingTowardsZero) {
                val oldOffset = currentOffset
                val newOffset = if (currentOffset > 0f) {
                    max(0f, currentOffset + delta)
                } else {
                    min(0f, currentOffset + delta)
                }
                snapOffset(newOffset)
                val consumedDelta = newOffset - oldOffset
                if (orientation == Orientation.Vertical) Offset(0f, consumedDelta) else Offset(consumedDelta, 0f)
            } else {
                applyOverScroll(delta)
                available
            }
        }

        override fun onPostScroll(
            consumed: Offset,
            available: Offset,
            source: NestedScrollSource
        ): Offset {
            if (source != NestedScrollSource.UserInput) return Offset.Zero

            val delta = if (orientation == Orientation.Vertical) available.y else available.x
            if (delta == 0f) return Offset.Zero

            applyOverScroll(delta)
            return available
        }

        override suspend fun onPreFling(available: Velocity): Velocity {
            if (currentOffset != 0f) {
                val velocity = if (orientation == Orientation.Vertical) available.y else available.x
                val pushingFurther =
                    (currentOffset > 0f && velocity > 0f) || (currentOffset < 0f && velocity < 0f)
                if (pushingFurther) {
                    triggerFlingSpring(velocity)
                } else {
                    triggerFlingSpring(0f)
                }
                return available
            }
            return Velocity.Zero
        }

        override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
            val velocity = if (orientation == Orientation.Vertical) available.y else available.x
            if (velocity != 0f) {
                triggerFlingSpring(velocity)
                return available
            }
            springBackIfNeeded()
            return Velocity.Zero
        }
    }
}

@Composable
fun rememberBounceOverscrollState(
    orientation: Orientation = Orientation.Vertical
): BounceOverscrollState {
    val scope = rememberCoroutineScope()
    return remember(scope, orientation) {
        BounceOverscrollState(scope, orientation)
    }
}