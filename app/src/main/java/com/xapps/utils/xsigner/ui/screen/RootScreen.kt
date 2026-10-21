package com.xapps.utils.xsigner.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RootScreen() {
    val behavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val expandedStyle = MaterialTheme.typography.headlineLarge

    val collapsedStyle = MaterialTheme.typography.titleLarge.copy()

    Scaffold(
        modifier = Modifier.nestedScroll(behavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    val isCollapsedSlot = LocalTextStyle.current.fontSize == MaterialTheme.typography.titleLarge.fontSize

                    Text(
                        text = "XSigner",
                        style = if (isCollapsedSlot) collapsedStyle else expandedStyle
                    )
                },
                scrollBehavior = behavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .clip(RoundedCornerShape(
                    topStart = 24.dp, topEnd = 24.dp
                ))
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .verticalScroll(rememberScrollState())
        ) { }
    }
}