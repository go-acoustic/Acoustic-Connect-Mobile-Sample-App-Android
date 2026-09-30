/*
 * Copyright (C) 2026 Acoustic, L.P. All rights reserved.
 *
 * Licensed under the Acoustic License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy
 * at https://www.acoustic.com/licenses/acoustic-license
 *
 * Sample app provided "as is", without warranty of any kind.
 */
package com.acoustic.connect.android.demo.connect.external.contract

/**
 * The logical page names each screen logs — the other half of the cross-platform contract. They
 * match what the React Native and iOS samples send, so one expected screen-view sequence holds for
 * every sample.
 *
 * The Compose sample uses these strings as its navigation routes, because the SDK's Compose
 * integration logs a destination's route as its screen name. The XML sample logs them explicitly
 * from each fragment.
 */
object ScreenName {
    const val PUSH = "Push"
    const val IDENTITY = "Identity"
    const val BEHAVIOUR = "Behaviour"
    const val SHOWCASE = "Showcase"
    const val VERIFICATION = "Verification"
    const val SCREEN_VIEWS = "Screen Views"
    const val WEBVIEW_POST = "WebView POST"

    /** Android only: the gesture targets moved here from their own tab. */
    const val GESTURES = "Gestures"

    /** Android only: the app-state readout moved here from its own tab. */
    const val APP_STATE = "App state"

    /**
     * How deep the Showcase detail chain may go. A few levels show the referrer advancing; an
     * unbounded stack is only a way to run out of memory.
     */
    const val MAX_SHOWCASE_DEPTH = 5

    /** `Showcase detail` for the first screen of the chain, then `Showcase detail 2` and so on. */
    fun showcaseDetail(depth: Int): String {
        require(depth in 1..MAX_SHOWCASE_DEPTH) { "depth $depth is outside 1..$MAX_SHOWCASE_DEPTH" }
        return if (depth == 1) "Showcase detail" else "Showcase detail $depth"
    }
}
