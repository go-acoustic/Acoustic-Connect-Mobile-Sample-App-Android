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

/**
 * Remembers which screen was logged last, so the next screen view can name it as its referrer.
 *
 * The referrer is the screen you came from, whichever way you came: pushing a screen names the one
 * below it, and going back names the one you just left. That is what the SDK's Compose integration
 * does on its own, and what the other platforms' samples send.
 */
class ScreenViewTrail {

    private var current: String? = null

    /** Records [screenName] as the current screen and returns the one before it, if any. */
    @Synchronized
    fun enter(screenName: String): String? {
        val referrer = current
        current = screenName
        return referrer
    }

    companion object {
        /** The app-wide trail. One per process, because a referrer can cross tabs. */
        val shared = ScreenViewTrail()
    }
}
