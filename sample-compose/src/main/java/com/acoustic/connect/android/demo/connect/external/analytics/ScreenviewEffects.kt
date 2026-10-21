/*
 * Copyright (C) 2026 Acoustic, L.P. All rights reserved.
 *
 * Licensed under the Acoustic License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy
 * at https://www.acoustic.com/licenses/acoustic-license
 *
 * Sample app provided "as is", without warranty of any kind.
 */
package com.acoustic.connect.android.demo.connect.external.analytics

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import com.acoustic.connect.android.connectmod.Connect
import com.acoustic.connect.android.connectmod.model.ConnectScreenviewType

/**
 * Logs a screenview UNLOAD for {@code screenName} when the composable leaves composition.
 *
 * <p>`ConnectWrapper` logs a LOAD for each route it navigates to, but no UNLOAD, so the app logs
 * the UNLOAD itself. Going back disposes the screen before the next route's LOAD is logged; earlier
 * SDKs dropped that UNLOAD as a repeat of the screen's own LOAD. The result is recorded
 * so the app shows whether the SDK queued it.
 */
@Composable
fun ScreenviewUnloadEffect(screenName: String) {
    val context = LocalContext.current
    DisposableEffect(screenName) {
        onDispose {
            (context as? Activity)?.let { activity ->
                // The return value is recorded rather than dropped: the audit needs to know whether
                // the SDK accepted the UNLOAD.
                val accepted = Connect.logScreenview(activity, screenName, ConnectScreenviewType.UNLOAD)
                SignalLog.record("screenviewUnload", screenName, accepted)
            }
        }
    }
}
