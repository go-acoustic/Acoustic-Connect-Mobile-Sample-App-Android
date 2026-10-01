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
 * The automation contract: every identifier a UI test may address, in one place.
 *
 * These are the React Native sample's `testID`s, byte for byte, so one page-object set drives the
 * React Native, iOS and both Android samples from the same locators. Compose publishes them as
 * `Modifier.testTag` with `testTagsAsResourceId` switched on at the root; the XML sample uses them
 * as `android:id` names. Either way Appium sees the same resource id.
 */
object SampleId {

    // Tabs. The analytics-only XML sample has no Push tab.
    const val TAB_NOTIFICATION = "tab_notification"
    const val TAB_IDENTITY = "tab_identity"
    const val TAB_BEHAVIOUR = "tab_behaviour"

    // Push. React Native carries no ids here; these predate the shared contract and the e2e
    // suite already depends on them.
    const val BTN_REQUEST_AUTHORIZATION = "btn_request_authorization"
    const val TXT_NOTIFICATION_AUTH_STATUS = "tv_notification_auth_status"

    // Identity.
    const val FIELD_IDENTIFIER_NAME = "et_identifier_name"
    const val FIELD_IDENTIFIER_VALUE = "et_identifier_value"
    const val BTN_SEND_IDENTITY_SIGNAL = "btn_send_identity_signal"
    const val BTN_SEND_ACCOUNT_REGISTERED_SIGNAL = "btn_send_account_registered_signal"
    /** Android only: React Native's Last Result card has no id. */
    const val TXT_IDENTITY_RESULT = "tv_identity_status_message"

    // Behaviour hub.
    const val BTN_OPEN_SHOWCASE = "btn_open_showcase"
    const val BTN_OPEN_VERIFICATION = "btn_open_verification"

    // Showcase.
    const val BTN_SHOWCASE_OPEN_DETAIL = "btn_showcase_open_detail"
    const val BTN_SHOWCASE_TAP = "btn_showcase_tap"
    const val TXT_SHOWCASE_TAPS = "txt_showcase_taps"
    const val FIELD_SHOWCASE_NOTE = "field_showcase_note"
    const val FIELD_SHOWCASE_SECRET = "field_showcase_secret"
    const val BTN_SEND_CUSTOM_EVENT = "btn_send_custom_event"
    const val TXT_CUSTOM_EVENT_RESULT = "txt_custom_event_result"
    const val BTN_SEND_NESTED_SIGNAL = "btn_send_nested_signal"
    const val BTN_SEND_FLAT_SIGNAL = "btn_send_flat_signal"
    const val TXT_SIGNAL_RESULT = "txt_signal_result"
    const val BTN_SHOWCASE_EXCEPTION = "btn_showcase_exception"
    const val TXT_SHOWCASE_EXCEPTION_RESULT = "txt_showcase_exception_result"
    const val BTN_SHOWCASE_DIALOG = "btn_showcase_dialog"
    const val TXT_SHOWCASE_DIALOG_RESULT = "txt_showcase_dialog_result"
    const val BTN_CAPTURE_DISABLE = "btn_capture_disable"
    const val BTN_CAPTURE_ENABLE = "btn_capture_enable"
    const val TXT_CAPTURE_STATE = "txt_capture_state"

    // Replay modals. React Native's modal card carries no ids at all, so these are the iOS
    // samples' names, proposed as the shared set in the tests repo's sample-app documentation.
    // One close id serves both modals because only one can be open at a time.
    const val BTN_OPEN_REPLAY_MODAL_OPAQUE = "btn_open_replay_modal_opaque"
    const val BTN_OPEN_REPLAY_MODAL_TRANSPARENT = "btn_open_replay_modal_transparent"
    const val FIELD_REPLAY_MODAL_NOTE = "field_replay_modal_note"
    const val BTN_REPLAY_MODAL_ACTION = "btn_replay_modal_action"
    const val BTN_CLOSE_REPLAY_MODAL = "btn_close_replay_modal"
    const val TXT_REPLAY_MODAL_RESULT = "txt_replay_modal_result"

    // Android only: the gesture and app-state screens, reached from the Showcase.
    const val BTN_OPEN_GESTURES = "btn_open_gestures"
    const val BTN_OPEN_APP_STATE = "btn_open_app_state"

    // Showcase detail.
    const val BTN_SHOWCASE_PUSH_DETAIL = "btn_showcase_push_detail"
    const val BTN_SHOWCASE_BACK = "btn_showcase_back"

    // Verification.
    const val BTN_IDENTITY_DEFAULTED = "btn_identity_defaulted"
    const val BTN_IDENTITY_EXPLICIT = "btn_identity_explicit"
    const val TXT_IDENTITY_DEFAULTS_RESULT = "txt_identity_defaults_result"
    const val FIELD_MASKED = "field_masked"
    const val A11Y_IMPLICIT = "a11y_implicit"
    const val A11Y_EXPLICIT = "a11y_explicit"
    const val A11Y_VALUE = "a11y_value"
    const val A11Y_FIELD = "a11y_field"

    // WebView POST. React Native also has `txt_webview_unavailable` for a host app
    // without a WebView; Android always has one, so like iOS it is not declared — a locator that
    // can never appear only invites a test that passes on a missing element.
    const val BTN_OPEN_WEBVIEW_POST = "btn_open_webview_post"
    const val BTN_WEBVIEW_SUBMIT = "btn_webview_submit"
    const val BTN_WEBVIEW_CAPTURE = "btn_webview_capture"
    const val BTN_WEBVIEW_RESET = "btn_webview_reset"
    const val WEBVIEW_POST = "webview_post"

    // Screen views.
    const val BTN_OPEN_SCREEN_VIEWS = "btn_open_screen_views"
    const val BTN_SCREENVIEW_SEND_ALL = "btn_screenview_send_all"
    const val TXT_SCREENVIEW_RESULT = "txt_screenview_result"
    const val BTN_CASE_RELOG = "btn_case_relog"
    const val TXT_CASE_RELOG_RESULT = "txt_case_relog_result"
    const val BTN_CASE_PUSH_NEXT = "btn_case_push_next"

    /** Direct-path button for one screen-view case, e.g. `btn_screenview_catalog`. */
    fun screenViewButton(caseId: String): String = "btn_screenview_$caseId"

    /** Navigation-path button for one screen-view case, e.g. `btn_navigate_catalog`. */
    fun navigateButton(caseId: String): String = "btn_navigate_$caseId"
}
