/*
 * Copyright (C) 2026 Acoustic, L.P. All rights reserved.
 *
 * Licensed under the Acoustic License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy
 * at https://www.acoustic.com/licenses/acoustic-license
 *
 * Sample app provided "as is", without warranty of any kind.
 */
package com.acoustic.connect.android.demo.connect.external

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import com.acoustic.connect.android.demo.connect.external.appstate.AppStateScreen
import com.acoustic.connect.android.demo.connect.external.behaviour.BehaviourScreen
import com.acoustic.connect.android.demo.connect.external.behaviour.ScreenViewCaseScreen
import com.acoustic.connect.android.demo.connect.external.behaviour.ScreenViewsScreen
import com.acoustic.connect.android.demo.connect.external.behaviour.ShowcaseDetailScreen
import com.acoustic.connect.android.demo.connect.external.behaviour.ShowcaseScreen
import com.acoustic.connect.android.demo.connect.external.behaviour.VerificationScreen
import com.acoustic.connect.android.demo.connect.external.behaviour.WebViewPostScreen
import com.acoustic.connect.android.demo.connect.external.behaviour.routableScreenViewCases
import com.acoustic.connect.android.demo.connect.external.contract.SampleId
import com.acoustic.connect.android.demo.connect.external.contract.ScreenName
import com.acoustic.connect.android.demo.connect.external.gestures.GestureScreen
import com.acoustic.connect.android.demo.connect.external.identity.IdentityScreen
import com.acoustic.connect.android.demo.connect.external.notification.NotificationScreen
import com.acoustic.connect.android.demo.connect.external.notification.NotificationViewModel
import com.acoustic.connect.android.demo.connect.external.ui.components.contractTag
import com.acoustic.connect.android.demo.connect.external.ui.theme.BrandBackground
import com.acoustic.connect.android.demo.connect.external.ui.theme.DarkGrey
import com.acoustic.connect.android.demo.connect.external.ui.theme.Periwinkle
import com.acoustic.connect.android.demo.connect.external.ui.theme.Violet
import com.acoustic.connect.android.demo.connect.external.shared.R as SharedR

/**
 * Route of the Behaviour tab's nested graph. A graph is never a destination itself, so this name
 * is never logged; the screens inside it are.
 */
private const val BEHAVIOUR_TAB = "behaviour_tab"

/*
 * Every other route is the screen's logged name. The SDK's Compose integration logs a destination's
 * route as its screen name and the previous route as the referrer, so naming the routes after the
 * shared contract is what makes this sample send the same screen views as the other platforms.
 */

private data class Tab(val route: String, val label: String, val icon: ImageVector, val tag: String)

private val tabs = listOf(
    Tab(ScreenName.PUSH, "Push", Icons.Filled.Notifications, SampleId.TAB_NOTIFICATION),
    Tab(ScreenName.IDENTITY, "Identity", Icons.Filled.Person, SampleId.TAB_IDENTITY),
    Tab(BEHAVIOUR_TAB, "Behaviour", Icons.Filled.Insights, SampleId.TAB_BEHAVIOUR),
)

/** Screens that sit on top of the Behaviour hub and so get a back arrow. */
private val stackedRoutes: Set<String> = setOf(
    ScreenName.SHOWCASE,
    ScreenName.VERIFICATION,
    ScreenName.WEBVIEW_POST,
    ScreenName.SCREEN_VIEWS,
    ScreenName.GESTURES,
    ScreenName.APP_STATE,
) + (1..ScreenName.MAX_SHOWCASE_DEPTH).map(ScreenName::showcaseDetail) +
    routableScreenViewCases.mapNotNull { it.name }

/**
 * The top-bar title for a route. Routes are the logged screen names, which read well as titles
 * except for the WebView screen, logged as `WebViewPost` like the React Native route it mirrors.
 */
@Composable
private fun titleFor(route: String?): String = when (route) {
    ScreenName.WEBVIEW_POST -> stringResource(SharedR.string.webview_title)
    else -> route.orEmpty()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavHostController, notificationViewModel: NotificationViewModel) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val destination = backStackEntry?.destination
    val route = destination?.route

    Scaffold(
        // Publishes every testTag below as a resource id, which is what Appium matches on.
        // A Dialog is a separate window, so ReplayModalCard switches it on again for its own.
        modifier = Modifier.semantics { testTagsAsResourceId = true },
        containerColor = BrandBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = titleFor(route),
                        color = Violet,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    if (route in stackedRoutes) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Periwinkle)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandBackground),
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                tabs.forEach { tab ->
                    val selected = destination?.hierarchy?.any { it.route == tab.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = { onTabSelected(navController, tab.route, selected) },
                        icon = { Icon(imageVector = tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Periwinkle,
                            selectedTextColor = Periwinkle,
                            indicatorColor = Color(0xFFF0EEFF),
                            unselectedIconColor = DarkGrey,
                            unselectedTextColor = DarkGrey,
                        ),
                        modifier = Modifier.contractTag(tab.tag),
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = ScreenName.PUSH,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(ScreenName.PUSH) {
                NotificationScreen(viewModel = notificationViewModel)
            }
            composable(ScreenName.IDENTITY) {
                IdentityScreen()
            }
            navigation(route = BEHAVIOUR_TAB, startDestination = ScreenName.BEHAVIOUR) {
                composable(ScreenName.BEHAVIOUR) {
                    BehaviourScreen(
                        onOpenShowcase = { navController.navigate(ScreenName.SHOWCASE) },
                        onOpenVerification = { navController.navigate(ScreenName.VERIFICATION) },
                    )
                }
                composable(ScreenName.SHOWCASE) {
                    ShowcaseScreen(onNavigate = { target -> navController.navigate(target) })
                }
                composable(ScreenName.VERIFICATION) {
                    VerificationScreen(onNavigate = { target -> navController.navigate(target) })
                }
                composable(ScreenName.WEBVIEW_POST) {
                    WebViewPostScreen()
                }
                composable(ScreenName.SCREEN_VIEWS) {
                    ScreenViewsScreen(onOpenCase = { case -> case.name?.let(navController::navigate) })
                }
                // One destination per case, routed by the case's name: the SDK logs the route, so
                // the route has to be the exact name under test.
                routableScreenViewCases.forEach { case ->
                    val route = case.name ?: return@forEach
                    composable(route) {
                        ScreenViewCaseScreen(
                            case = case,
                            onPush = { next -> next.name?.let(navController::navigate) },
                        )
                    }
                }
                // One destination per depth rather than one with an argument: the SDK logs the
                // route pattern, so an argument would log the placeholder instead of the name.
                (1..ScreenName.MAX_SHOWCASE_DEPTH).forEach { depth ->
                    composable(ScreenName.showcaseDetail(depth)) {
                        ShowcaseDetailScreen(
                            depth = depth,
                            onPushNext = { navController.navigate(ScreenName.showcaseDetail(depth + 1)) },
                            onBack = { navController.popBackStack() },
                        )
                    }
                }
                composable(ScreenName.GESTURES) {
                    GestureScreen()
                }
                composable(ScreenName.APP_STATE) {
                    AppStateScreen()
                }
            }
        }
    }
}

/**
 * Switches tabs keeping each tab's own stack, and — like React Navigation's bottom tabs — pressing
 * the tab you are already on pops its stack back to the root.
 */
private fun onTabSelected(navController: NavHostController, route: String, alreadySelected: Boolean) {
    if (alreadySelected) {
        if (route == BEHAVIOUR_TAB) navController.popBackStack(ScreenName.BEHAVIOUR, inclusive = false)
        return
    }
    navController.navigate(route) {
        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
