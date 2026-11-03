package com.xapps.utils.xsigner.ui.widget

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

sealed interface SettingItemSpec {
    val id: String
    val title: String?
    val description: String?
    val enabled: Boolean

    data class Switch(
        override val id: String,
        override val title: String,
        override val description: String? = null,
        override val enabled: Boolean = true,
        val checked: Boolean,
        val onCheckedChange: (Boolean) -> Unit
    ) : SettingItemSpec

    data class Navigation(
        override val id: String,
        override val title: String,
        override val description: String? = null,
        override val enabled: Boolean = true,
        val valueText: String? = null,
        val onClick: () -> Unit
    ) : SettingItemSpec

    data class Split(
        override val id: String,
        override val title: String,
        override val description: String? = null,
        override val enabled: Boolean = true,
        val checked: Boolean,
        val onCheckedChange: (Boolean) -> Unit,
        val onDetailClick: () -> Unit
    ) : SettingItemSpec

    data class Custom(
        override val id: String,
        override val title: String? = null,
        override val description: String? = null,
        override val enabled: Boolean = true,
        val content: @Composable ColumnScope.() -> Unit
    ) : SettingItemSpec
}

data class SettingGroupSpec(
    val id: String,
    val title: String? = null,
    val items: List<SettingItemSpec>
)

@Composable
fun animatedSegmentedItemShape(
    index: Int,
    count: Int,
    largeRadius: Dp = 24.dp,
    smallRadius: Dp = 4.dp
): Shape {
    val animSpec = tween<Dp>(durationMillis = 150)

    val topStart = animateDpAsState(
        targetValue = if (index == 0) largeRadius else smallRadius,
        animationSpec = animSpec,
        label = "topStart"
    )
    val topEnd = animateDpAsState(
        targetValue = if (index == 0) largeRadius else smallRadius,
        animationSpec = animSpec,
        label = "topEnd"
    )
    val bottomStart = animateDpAsState(
        targetValue = if (index == count - 1) largeRadius else smallRadius,
        animationSpec = animSpec,
        label = "bottomStart"
    )
    val bottomEnd = animateDpAsState(
        targetValue = if (index == count - 1) largeRadius else smallRadius,
        animationSpec = animSpec,
        label = "bottomEnd"
    )

    return RoundedCornerShape(
        topStart = topStart.value,
        topEnd = topEnd.value,
        bottomStart = bottomStart.value,
        bottomEnd = bottomEnd.value
    )
}

@Composable
fun segmentedItemShape(index: Int, count: Int, largeRadius: Dp = 24.dp, smallRadius: Dp = 4.dp): Shape =
    when {
        count == 1 -> RoundedCornerShape(largeRadius)
        index == 0 -> RoundedCornerShape(topStart = largeRadius, topEnd = largeRadius, bottomStart = smallRadius, bottomEnd = smallRadius)
        index == count - 1 -> RoundedCornerShape(topStart = smallRadius, topEnd = smallRadius, bottomStart = largeRadius, bottomEnd = largeRadius)
        else -> RoundedCornerShape(smallRadius)
    }

@Composable
fun ExpressiveSettingsList(
    groups: List<SettingGroupSpec>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        groups.forEach { group ->
            group.title?.let { title ->
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 32.dp, top = 8.dp, bottom = 2.dp)
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.animateContentSize()
            ) {
                group.items.forEachIndexed { index, item ->
                    key(item.id) {
                        when (item) {
                            is SettingItemSpec.Switch -> RenderSwitchItem(item, index, group.items.size)
                            is SettingItemSpec.Navigation -> RenderNavigationItem(item, index, group.items.size)
                            is SettingItemSpec.Split -> RenderSplitItem(item, index, group.items.size)
                            is SettingItemSpec.Custom -> RenderCustomItem(item, index, group.items.size)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RenderSwitchItem(item: SettingItemSpec.Switch, index: Int, count: Int) {
    val shape = animatedSegmentedItemShape(index, count)
    Surface(
        shape = shape,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(shape)
            .clickable(enabled = item.enabled) { item.onCheckedChange(!item.checked) }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 16.dp)
            ) {
                Text(item.title, style = MaterialTheme.typography.titleMedium)
                item.description?.let {
                    Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            ExpressiveSwitch(checked = item.checked, enabled = item.enabled)
        }
    }
}

@Composable
private fun RenderNavigationItem(item: SettingItemSpec.Navigation, index: Int, count: Int) {
    val shape = animatedSegmentedItemShape(index, count)
    Surface(
        shape = shape,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(shape)
            .clickable(enabled = item.enabled, onClick = item.onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 16.dp)
            ) {
                Text(item.title, style = MaterialTheme.typography.titleMedium)
                item.description?.let {
                    Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            item.valueText?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(end = 8.dp))
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun RenderSplitItem(item: SettingItemSpec.Split, index: Int, count: Int) {
    val shape = animatedSegmentedItemShape(index, count)
    Surface(
        shape = shape,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(shape)
            .clickable(enabled = item.enabled, onClick = item.onDetailClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp)
            ) {
                Text(item.title, style = MaterialTheme.typography.titleMedium)
                item.description?.let {
                    Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            VerticalDivider(
                modifier = Modifier
                    .height(32.dp)
                    .padding(horizontal = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable(
                        enabled = item.enabled
                    ) { item.onCheckedChange(!item.checked) }
                    .padding(4.dp)
            ) {
                ExpressiveSwitch(checked = item.checked, enabled = item.enabled)
            }
        }
    }
}

@Composable
private fun RenderCustomItem(item: SettingItemSpec.Custom, index: Int, count: Int) {
    val shape = animatedSegmentedItemShape(index, count)
    Surface(
        shape = shape,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(shape)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (item.title != null) {
                Column(modifier = Modifier.padding(bottom = 8.dp, end = 16.dp)) {
                    Text(item.title, style = MaterialTheme.typography.titleMedium)
                    item.description?.let {
                        Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            item.content(this)
        }
    }
}

@Composable
fun ExpressiveSwitch(
    checked: Boolean,
    enabled: Boolean = true
) {
    Switch(
        checked = checked,
        onCheckedChange = null,
        enabled = enabled,
        thumbContent = {
            if (checked) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    modifier = Modifier.size(SwitchDefaults.IconSize)
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = null,
                    modifier = Modifier.size(SwitchDefaults.IconSize)
                )
            }
        }
    )
}
