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

import com.acoustic.connect.android.demo.connect.external.behaviour.ScreenViewCases
import com.acoustic.connect.android.demo.connect.external.contract.SampleId
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Holds the XML sample's resources to the shared automation contract. In this app the contract ids
 * are `android:id` names, so a typo in a layout would pass every build and only fail on a device;
 * this test fails it here instead.
 */
class ContractIdsTest {

    /**
     * Every id declared anywhere in the app's resources — layouts, the tab menu, the nav graph, and
     * `ids.xml` for the buttons built in code.
     */
    private val declaredIds: Set<String> = listOf("layout", "menu", "navigation", "values")
        .flatMap { File("src/main/res/$it").listFiles().orEmpty().toList() }
        .flatMap { file ->
            val text = file.readText()
            Regex("@\\+id/([a-z0-9_]+)").findAll(text).map { it.groupValues[1] } +
                Regex("<item name=\"([a-z0-9_]+)\" type=\"id\"").findAll(text).map { it.groupValues[1] }
        }
        .toSet()

    /** The contract ids this app renders. The analytics-only sample has no Push tab. */
    private val rendered = listOf(
        SampleId.TAB_IDENTITY,
        SampleId.TAB_BEHAVIOUR,
        SampleId.FIELD_IDENTIFIER_NAME,
        SampleId.FIELD_IDENTIFIER_VALUE,
        SampleId.BTN_SEND_IDENTITY_SIGNAL,
        SampleId.BTN_SEND_ACCOUNT_REGISTERED_SIGNAL,
        SampleId.TXT_IDENTITY_RESULT,
        SampleId.BTN_OPEN_SHOWCASE,
        SampleId.BTN_SHOWCASE_OPEN_DETAIL,
        SampleId.BTN_SHOWCASE_TAP,
        SampleId.TXT_SHOWCASE_TAPS,
        SampleId.FIELD_SHOWCASE_NOTE,
        SampleId.FIELD_SHOWCASE_SECRET,
        SampleId.BTN_SEND_CUSTOM_EVENT,
        SampleId.TXT_CUSTOM_EVENT_RESULT,
        SampleId.BTN_SEND_NESTED_SIGNAL,
        SampleId.BTN_SEND_FLAT_SIGNAL,
        SampleId.TXT_SIGNAL_RESULT,
        SampleId.BTN_SHOWCASE_EXCEPTION,
        SampleId.TXT_SHOWCASE_EXCEPTION_RESULT,
        SampleId.BTN_SHOWCASE_DIALOG,
        SampleId.TXT_SHOWCASE_DIALOG_RESULT,
        SampleId.BTN_OPEN_REPLAY_MODAL_OPAQUE,
        SampleId.BTN_OPEN_REPLAY_MODAL_TRANSPARENT,
        SampleId.FIELD_REPLAY_MODAL_NOTE,
        SampleId.BTN_REPLAY_MODAL_ACTION,
        SampleId.BTN_CLOSE_REPLAY_MODAL,
        SampleId.TXT_REPLAY_MODAL_RESULT,
        SampleId.BTN_OPEN_GESTURES,
        SampleId.BTN_OPEN_APP_STATE,
        SampleId.BTN_CAPTURE_DISABLE,
        SampleId.BTN_CAPTURE_ENABLE,
        SampleId.TXT_CAPTURE_STATE,
        SampleId.BTN_SHOWCASE_PUSH_DETAIL,
        SampleId.BTN_SHOWCASE_BACK,
        SampleId.BTN_OPEN_VERIFICATION,
        SampleId.BTN_IDENTITY_DEFAULTED,
        SampleId.BTN_IDENTITY_EXPLICIT,
        SampleId.TXT_IDENTITY_DEFAULTS_RESULT,
        SampleId.FIELD_MASKED,
        SampleId.A11Y_IMPLICIT,
        SampleId.A11Y_EXPLICIT,
        SampleId.A11Y_VALUE,
        SampleId.A11Y_FIELD,
        SampleId.BTN_OPEN_WEBVIEW_POST,
        SampleId.BTN_WEBVIEW_SUBMIT,
        SampleId.BTN_WEBVIEW_CAPTURE,
        SampleId.BTN_WEBVIEW_RESET,
        SampleId.WEBVIEW_POST,
        SampleId.BTN_OPEN_SCREEN_VIEWS,
        SampleId.BTN_SCREENVIEW_SEND_ALL,
        SampleId.TXT_SCREENVIEW_RESULT,
        SampleId.BTN_CASE_RELOG,
        SampleId.TXT_CASE_RELOG_RESULT,
        SampleId.BTN_CASE_PUSH_NEXT,
    ) + ScreenViewCases.NAV.map { SampleId.navigateButton(it.id) } +
        ScreenViewCases.DIRECT.map { SampleId.screenViewButton(it.id) }

    @Test
    fun `every contract id this app renders is declared in its resources`() {
        val missing = rendered.filterNot { it in declaredIds }
        assertTrue("contract ids with no android:id: $missing", missing.isEmpty())
    }

    @Test
    fun `the analytics-only app declares no push ids`() {
        val push = listOf(SampleId.TAB_NOTIFICATION, SampleId.BTN_REQUEST_AUTHORIZATION, SampleId.TXT_NOTIFICATION_AUTH_STATUS)
        val present = push.filter { it in declaredIds }
        assertTrue("push ids in the analytics-only app: $present", present.isEmpty())
    }
}
