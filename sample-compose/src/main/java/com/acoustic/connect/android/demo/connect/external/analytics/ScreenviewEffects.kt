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
 * <p>Measured behaviour (SDK 11.0.8-beta): this call is rejected — `logScreenview`
 * returns false for every screen, so no UNLOAD reaches the collector. `ConnectWrapper` does not
 * make up for it: it emits a LOAD when the composition starts, not on route change, so navigating
 * between tabs produces no screenview at all. The call is kept, and its result recorded, so the
 * gap stays visible in the app rather than only in a collector query.
 */
@Composable
fun ScreenviewUnloadEffect(screenName: String) {
    val context = LocalContext.current
    DisposableEffect(screenName) {
        onDispose {
            (context as? Activity)?.let { activity ->
                // The return value is recorded rather than dropped: the audit needs to know whether
                // the SDK accepted the UNLOAD, and no UNLOAD ever reached the collector.
                val accepted = Connect.logScreenview(activity, screenName, ConnectScreenviewType.UNLOAD)
                SignalLog.record("screenviewUnload", screenName, accepted)
            }
        }
    }
}
