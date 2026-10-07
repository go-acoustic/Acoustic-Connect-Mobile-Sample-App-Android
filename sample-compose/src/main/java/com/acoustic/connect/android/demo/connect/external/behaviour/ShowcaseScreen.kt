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

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.acoustic.connect.android.demo.connect.external.R
import com.acoustic.connect.android.demo.connect.external.analytics.ScreenviewUnloadEffect
import com.acoustic.connect.android.demo.connect.external.contract.SampleId
import com.acoustic.connect.android.demo.connect.external.contract.ScreenName
import com.acoustic.connect.android.demo.connect.external.ui.components.BodyText
import com.acoustic.connect.android.demo.connect.external.ui.components.DemoCard
import com.acoustic.connect.android.demo.connect.external.ui.components.DemoScreen
import com.acoustic.connect.android.demo.connect.external.ui.components.DemoTextField
import com.acoustic.connect.android.demo.connect.external.ui.components.HintText
import com.acoustic.connect.android.demo.connect.external.ui.components.LogoHeader
import com.acoustic.connect.android.demo.connect.external.ui.components.MonoText
import com.acoustic.connect.android.demo.connect.external.ui.components.NoteBox
import com.acoustic.connect.android.demo.connect.external.ui.components.PrimaryButton
import com.acoustic.connect.android.demo.connect.external.ui.components.SecondaryButton
import com.acoustic.connect.android.demo.connect.external.shared.R as SharedR

/**
 * The general-purpose demo of the SDK's behaviour capture, for someone integrating it for the
 * first time. Reached from the Behaviour hub.
 *
 * Ordering follows what an integrator meets first: what ConnectWrapper captures with no code
 * (screen views, taps, text), then the explicit logging calls (custom events, signals, exceptions),
 * then dialogs and modals, and finally runtime control. The Gestures and App state entries are
 * Android additions; the other cards match the React Native and iOS samples.
 */
@Composable
fun ShowcaseScreen(onNavigate: (String) -> Unit) {
    ScreenviewUnloadEffect(ScreenName.SHOWCASE)

    DemoScreen {
        LogoHeader(title = stringResource(SharedR.string.showcase_header))

        DemoCard(title = stringResource(SharedR.string.showcase_read_title)) {
            BodyText(stringResource(SharedR.string.showcase_read_body))
            BodyText(stringResource(R.string.showcase_read_capture))
        }

        DemoCard(title = stringResource(SharedR.string.showcase_screen_views_title)) {
            BodyText(stringResource(R.string.showcase_screen_views_body))
            PrimaryButton(
                title = stringResource(SharedR.string.showcase_open_detail),
                tag = SampleId.BTN_SHOWCASE_OPEN_DETAIL,
                onClick = { onNavigate(ScreenName.showcaseDetail(1)) },
            )
        }

        ClickCaptureCard()

        DemoCard(title = stringResource(SharedR.string.showcase_gestures_title)) {
            BodyText(stringResource(SharedR.string.showcase_gestures_body))
            SecondaryButton(
                title = stringResource(SharedR.string.showcase_open_gestures),
                tag = SampleId.BTN_OPEN_GESTURES,
                onClick = { onNavigate(ScreenName.GESTURES) },
            )
        }

        TextCaptureCard()

        DemoCard(title = stringResource(SharedR.string.showcase_custom_event_title)) {
            BodyText(stringResource(SharedR.string.showcase_custom_event_body))
            CustomEventBody()
        }

        SignalCard()

        ExceptionCard()

        DialogCard()

        ReplayModalCard(transparent = false)

        ReplayModalCard(transparent = true)

        DemoCard(title = stringResource(SharedR.string.showcase_app_state_title)) {
            BodyText(stringResource(SharedR.string.showcase_app_state_body))
            SecondaryButton(
                title = stringResource(SharedR.string.showcase_open_app_state),
                tag = SampleId.BTN_OPEN_APP_STATE,
                onClick = { onNavigate(ScreenName.APP_STATE) },
            )
        }

        CaptureControlCard()
    }
}

/**
 * The id the tap target carries in the captured layout. A `LoggedButton` is identified there by its
 * button text and the first eight characters of its log identifier, which [PrimaryButton] sets to
 * the contract id.
 */
private const val TAP_CONTROL_ID = "button_Tap_me_btn_show"

@Composable
private fun ClickCaptureCard() {
    var taps by rememberSaveable { mutableIntStateOf(0) }
    DemoCard(title = stringResource(SharedR.string.showcase_taps_title)) {
        BodyText(stringResource(R.string.showcase_taps_body))
        PrimaryButton(
            title = stringResource(SharedR.string.showcase_tap_me),
            tag = SampleId.BTN_SHOWCASE_TAP,
            onClick = { taps += 1 },
        )
        MonoText(
            stringResource(R.string.showcase_taps_result, taps, TAP_CONTROL_ID),
            tag = SampleId.TXT_SHOWCASE_TAPS,
        )
    }
}

@Composable
private fun TextCaptureCard() {
    var note by rememberSaveable { mutableStateOf("") }
    var secret by rememberSaveable { mutableStateOf("") }
    val maskedLabel = stringResource(SharedR.string.showcase_masked_field)
    DemoCard(title = stringResource(SharedR.string.showcase_text_title)) {
        BodyText(stringResource(R.string.showcase_text_body))
        DemoTextField(
            label = stringResource(SharedR.string.showcase_plain_field),
            placeholder = stringResource(SharedR.string.showcase_plain_placeholder),
            value = note,
            tag = SampleId.FIELD_SHOWCASE_NOTE,
            onValueChange = { note = it },
        )
        DemoTextField(
            label = maskedLabel,
            placeholder = stringResource(SharedR.string.showcase_masked_placeholder),
            value = secret,
            tag = SampleId.FIELD_SHOWCASE_SECRET,
            // Named in ConnectLayoutConfig.json's MaskAccessibilityLabelList, which is how the
            // Compose capture decides whether to mask a field.
            maskLabel = maskedLabel,
            onValueChange = { secret = it },
        )
        NoteBox { HintText(stringResource(R.string.showcase_text_hint)) }
    }
}

/** The custom-event body; the Verification screen reuses it inside its own card frame. */
@Composable
fun CustomEventBody() {
    var result by rememberSaveable { mutableStateOf<String?>(null) }
    NoteBox { MonoText(ShowcaseActions.CUSTOM_EVENT_PAYLOAD_TEXT) }
    PrimaryButton(
        title = stringResource(SharedR.string.showcase_send_custom_event),
        tag = SampleId.BTN_SEND_CUSTOM_EVENT,
        onClick = { result = ShowcaseActions.sendCustomEvent() },
    )
    result?.let { MonoText(it, tag = SampleId.TXT_CUSTOM_EVENT_RESULT) }
}

/** The signal card; the Verification screen reuses it. */
@Composable
fun SignalCard() {
    var result by rememberSaveable { mutableStateOf<String?>(null) }
    DemoCard(title = stringResource(SharedR.string.showcase_signal_title)) {
        BodyText(stringResource(SharedR.string.showcase_signal_body))
        PrimaryButton(
            title = stringResource(SharedR.string.showcase_send_nested_signal),
            tag = SampleId.BTN_SEND_NESTED_SIGNAL,
            onClick = { result = ShowcaseActions.sendNestedSignal() },
        )
        SecondaryButton(
            title = stringResource(SharedR.string.showcase_send_flat_signal),
            tag = SampleId.BTN_SEND_FLAT_SIGNAL,
            onClick = { result = ShowcaseActions.sendFlatSignal() },
        )
        result?.let { NoteBox { MonoText(it, tag = SampleId.TXT_SIGNAL_RESULT) } }
    }
}

@Composable
private fun ExceptionCard() {
    var result by rememberSaveable { mutableStateOf<String?>(null) }
    DemoCard(title = stringResource(SharedR.string.showcase_exception_title)) {
        BodyText(stringResource(SharedR.string.showcase_exception_body))
        PrimaryButton(
            title = stringResource(SharedR.string.showcase_log_exception),
            tag = SampleId.BTN_SHOWCASE_EXCEPTION,
            onClick = { result = ShowcaseActions.logHandledException() },
        )
        result?.let { MonoText(it, tag = SampleId.TXT_SHOWCASE_EXCEPTION_RESULT) }
    }
}

@Composable
private fun DialogCard() {
    val activity = LocalContext.current as? Activity
    var last by rememberSaveable { mutableStateOf<String?>(null) }
    DemoCard(title = stringResource(SharedR.string.showcase_dialog_title)) {
        BodyText(stringResource(SharedR.string.showcase_dialog_body))
        PrimaryButton(
            title = stringResource(SharedR.string.showcase_show_dialog),
            tag = SampleId.BTN_SHOWCASE_DIALOG,
            onClick = { activity?.let { ShowcaseActions.showDialog(it) { outcome -> last = outcome } } },
        )
        last?.let { MonoText(it, tag = SampleId.TXT_SHOWCASE_DIALOG_RESULT) }
    }
}

/** Runtime capture control; the Verification screen reuses it. */
@Composable
fun CaptureControlCard() {
    val activity = LocalContext.current as? Activity
    var state by rememberSaveable { mutableStateOf<String?>(null) }
    DemoCard(title = stringResource(SharedR.string.showcase_capture_title)) {
        BodyText(stringResource(SharedR.string.showcase_capture_body))
        PrimaryButton(
            title = stringResource(SharedR.string.showcase_capture_disable),
            tag = SampleId.BTN_CAPTURE_DISABLE,
            onClick = { state = ShowcaseActions.disableSdk() },
        )
        PrimaryButton(
            title = stringResource(SharedR.string.showcase_capture_enable),
            tag = SampleId.BTN_CAPTURE_ENABLE,
            onClick = { activity?.let { state = ShowcaseActions.enableSdk(it) } },
        )
        state?.let { MonoText(it, tag = SampleId.TXT_CAPTURE_STATE) }
    }
}
