/*
 * Copyright (C) 2026 Acoustic, L.P. All rights reserved.
 *
 * Licensed under the Acoustic License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy
 * at https://www.acoustic.com/licenses/acoustic-license
 *
 * Sample app provided "as is", without warranty of any kind.
 */
package com.acoustic.connect.android.demo.connect.external.behaviour

import org.junit.Assert.assertEquals
import org.junit.Test

class ScenariosTest {

    @Test
    fun `the registry holds the ten shared scenarios in order`() {
        assertEquals(
            listOf(
                "custom-event-value-types", "signal-nested-json", "identity-login-method-default",
                "layout-config-applied", "android-compile-classpath", "replay-captures-modal",
                "screenview-referrer", "webview-post-not-replayed-as-get",
                "accessibility-label-masking", "masking-value-id-rules",
            ),
            Scenarios.ALL.map { it.key },
        )
    }

    @Test
    fun `only the WebView fix is still marked as a baseline`() {
        // screenview-referrer lost its banner once Connect iOS 2.1.37 shipped the fix, as on iOS.
        // The WebView one stays: the published fix misses a form submitted before the first capture.
        assertEquals(
            listOf("webview-post-not-replayed-as-get"),
            Scenarios.ALL.filter { it.blockedBy != null }.map { it.key },
        )
    }

    @Test
    fun `Android and iOS native fixes are tracked on separate channels`() {
        assertEquals(ScenarioChannel.ANDROID_NATIVE, Scenarios.WEBVIEW_POST_NOT_REPLAYED_AS_GET.channel)
        assertEquals(ScenarioChannel.IOS_NATIVE, Scenarios.SCREENVIEW_REFERRER.channel)
        assertEquals(ScenarioPlatform.ANDROID, Scenarios.ANDROID_COMPILE_CLASSPATH.platform)
    }

    @Test
    fun `channel labels match the other samples`() {
        assertEquals(
            listOf(
                "React Native SDK", "iOS native SDK", "Android native SDK",
                "iOS + Android native SDK", "Build-time",
            ),
            ScenarioChannel.entries.map { it.label },
        )
    }
}
