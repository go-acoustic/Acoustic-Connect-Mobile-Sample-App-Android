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

import android.app.Application
import com.acoustic.connect.android.demo.connect.external.analytics.AppStateSignals

/**
 * Registers the process-level analytics observers.
 *
 * <p>The SDK itself is still initialised by `ConnectComposeUI.ConnectWrapper` in [MainActivity] —
 * that is the integration path this sample is meant to demonstrate. Only the app-state observers
 * live here, because foreground/background and orientation are process facts that no single
 * activity or composition can see.
 */
class ConnectDemoApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        AppStateSignals.install(this)
    }
}
