package com.xapps.utils.xsigner.ui.screen

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.xapps.utils.xsigner.ui.widget.ExpressiveFloatingNavBar
import com.xapps.utils.xsigner.ui.widget.FabStyle
import com.xapps.utils.xsigner.ui.widget.NavTabItem

sealed class NavDestination(
    val route: String,
    val title: String,
    val icon: ImageVector,
) {
    data object Signing : NavDestination("signing", "Signer", Icons.Rounded.Home)
    data object Keys : NavDestination("keys", "Keys", Icons.Rounded.Key)
    data object Settings : NavDestination("settings", "Settings", Icons.Rounded.Settings)

    companion object {
        val bottomNavDestinations = listOf(Signing, Keys, Settings)
    }
}

private fun getDestinationIndex(route: String?): Int {
    return when (route) {
        NavDestination.Signing.route -> 0
        NavDestination.Keys.route -> 1
        NavDestination.Settings.route -> 2
        else -> 0
    }
}

private val sharedAxisEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    val initialIndex = getDestinationIndex(initialState.destination.route)
    val targetIndex = getDestinationIndex(targetState.destination.route)
    val direction = if (targetIndex >= initialIndex) 1 else -1

    slideInHorizontally(
        initialOffsetX = { fullWidth -> direction * (fullWidth * 0.15f).toInt() },
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
    ) + fadeIn(
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
    )
}

private val sharedAxisExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    val initialIndex = getDestinationIndex(initialState.destination.route)
    val targetIndex = getDestinationIndex(targetState.destination.route)
    val direction = if (targetIndex >= initialIndex) -1 else 1

    slideOutHorizontally(
        targetOffsetX = { fullWidth -> direction * (fullWidth * 0.15f).toInt() },
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
    ) + fadeOut(
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
    )
}

@Composable
@Preview
fun RootScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val navigationItems = remember {
        NavDestination.bottomNavDestinations.map { destination ->
            NavTabItem(destination.icon, destination.title)
        }
    }

    val selectedIndex = remember(currentRoute) {
        val index = NavDestination.bottomNavDestinations.indexOfFirst { it.route == currentRoute }
        if (index >= 0) index else 0
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        NavHost(
            navController = navController,
            startDestination = NavDestination.Signing.route,
            modifier = Modifier.fillMaxSize(),
            enterTransition = sharedAxisEnter,
            exitTransition = sharedAxisExit,
            popEnterTransition = sharedAxisEnter,
            popExitTransition = sharedAxisExit,
            predictivePopEnterTransition = { sharedAxisEnter() },
            predictivePopExitTransition = { sharedAxisExit() },
        ) {
            composable(NavDestination.Signing.route) {
                SigningScreen()
            }
            composable(NavDestination.Keys.route) {
                KeysScreen()
            }
            composable(NavDestination.Settings.route) {
                SettingsScreen()
            }
        }

        ExpressiveFloatingNavBar(
            items = navigationItems,
            selectedIndex = selectedIndex,
            onTabSelected = { index ->
                val targetDestination = NavDestination.bottomNavDestinations.getOrNull(index)
                if ((targetDestination != null) && (targetDestination.route != currentRoute)) {
                    navController.navigate(targetDestination.route) {
                        popUpTo(NavDestination.Signing.route) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            },
            showFab = selectedIndex == 1,
            fabStyle = FabStyle.primaryStyle(
                icon = Icons.Rounded.Add,
                contentDescription = "Add Key",
            ),
            onFabClick = {},
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 12.dp),
        )
    }
}