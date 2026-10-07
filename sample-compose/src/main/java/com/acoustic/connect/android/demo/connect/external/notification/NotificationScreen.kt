/*
 * Copyright (C) 2026 Acoustic, L.P. All rights reserved.
 *
 * Licensed under the Acoustic License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy
 * at https://www.acoustic.com/licenses/acoustic-license
 *
 * Sample app provided "as is", without warranty of any kind.
 */
package com.acoustic.connect.android.demo.connect.external.notification

import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.acoustic.connect.android.connectmod.Connect
import com.acoustic.connect.android.connectmod.composeui.customcomposable.LoggedText
import com.acoustic.connect.android.demo.connect.external.analytics.ScreenviewUnloadEffect
import com.acoustic.connect.android.demo.connect.external.contract.SampleId
import com.acoustic.connect.android.demo.connect.external.contract.ScreenName
import com.acoustic.connect.android.demo.connect.external.ui.components.DemoCard
import com.acoustic.connect.android.demo.connect.external.ui.components.DemoScreen
import com.acoustic.connect.android.demo.connect.external.ui.components.LogoHeader
import com.acoustic.connect.android.demo.connect.external.ui.components.PrimaryButton
import com.acoustic.connect.android.demo.connect.external.ui.components.contractTag
import com.acoustic.connect.android.demo.connect.external.ui.theme.AcousticGreen
import com.acoustic.connect.android.demo.connect.external.ui.theme.BrandGreen
import com.acoustic.connect.android.demo.connect.external.ui.theme.DarkGrey
import com.acoustic.connect.android.demo.connect.external.ui.theme.MiddleGrey

@Composable
fun NotificationScreen(
    viewModel: NotificationViewModel = viewModel(),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val activity = LocalContext.current as? ComponentActivity

    ScreenviewUnloadEffect(ScreenName.PUSH)

    LifecycleResumeEffect(Unit) {
        viewModel.refreshAuthorization()
        onPauseOrDispose { }
    }

    DemoScreen(modifier = modifier) {
        LogoHeader(title = "Push Demo")
        NotificationAuthorizationCard(
            authorized = uiState.isNotificationAuthorized,
            statusMessage = uiState.notificationStatusMessage,
            isStatusMessageError = uiState.isError,
            isSdkEnabled = uiState.isSdkEnabled,
            onRequestAuthorization = {
                activity?.let { Connect.push.requestNotificationPermission(it) }
            },
        )
    }
}

@Composable
private fun NotificationAuthorizationCard(
    authorized: Boolean,
    statusMessage: String = "",
    // Drives the status-message color. Not derived from statusMessage's text — a prior version
    // matched on "startsWith(\"Error\")" / "contains(\"disabled\")" and silently rendered any
    // future error message green if its wording ever changed.
    isStatusMessageError: Boolean = false,
    isSdkEnabled: Boolean,
    onRequestAuthorization: () -> Unit,
) {
    DemoCard(title = "Notification Authorization") {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(if (authorized) BrandGreen else MiddleGrey, shape = CircleShape)
                    .contractTag("iv_notification_auth_status_dot"),
            )
            LoggedText(
                text = "Status: ${if (authorized) "Authorized" else "Not Authorized"}",
                color = DarkGrey,
                modifier = Modifier.contractTag(SampleId.TXT_NOTIFICATION_AUTH_STATUS),
            )
        }

        PrimaryButton(
            title = "Request Authorization",
            tag = SampleId.BTN_REQUEST_AUTHORIZATION,
            enabled = isSdkEnabled,
            onClick = onRequestAuthorization,
        )

        if (statusMessage.isNotEmpty()) {
            LoggedText(
                text = statusMessage,
                style = MaterialTheme.typography.bodySmall,
                color = if (isStatusMessageError) MaterialTheme.colorScheme.error else AcousticGreen,
            )
        }
    }
}
