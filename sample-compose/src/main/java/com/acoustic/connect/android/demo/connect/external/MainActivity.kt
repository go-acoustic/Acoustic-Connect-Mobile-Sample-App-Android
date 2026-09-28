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

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.navigation.compose.rememberNavController
import com.acoustic.connect.android.connectmod.composeui.ConnectComposeUI
import com.acoustic.connect.android.connectmod.push.ConnectPushConfig
import com.acoustic.connect.android.connectmod.push.constants.ConnectConstants
import com.acoustic.connect.android.connectmod.push.core.MobileServiceType
import com.acoustic.connect.android.demo.connect.external.notification.NotificationViewModel
import com.acoustic.connect.android.demo.connect.external.ui.theme.ConnectApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: NotificationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val credentials = AcousticCredentials.load(this)

        setContent {
            ConnectApplicationTheme {
                val navController = rememberNavController()

                ConnectComposeUI.ConnectWrapper(
                    navController = navController,
                    appKey = credentials.appKey,
                    postMessageURL = credentials.collectorUrl,
                    pushConfig = ConnectPushConfig(
                        application = application,
                        iconRes = R.drawable.ic_notification,
                        onFailure = { exception ->
                            Log.e(TAG, "ConnectPush initialization failed: ${exception.message}")
                        },
                        onTokenReady = {
                            token -> viewModel.setToken(token)
                            Log.d("Token", "Token: "+token.token)
                        },
                        onPermissionResult = { isGranted ->
                            viewModel.onNotificationPermissionResult(isGranted)
                        },
                    ),
                ) {
                    MainScreen(navController = navController, notificationViewModel = viewModel)
                }
            }
        }
    }

    companion object {
        private const val TAG = "ConnectDemo"
    }
}
