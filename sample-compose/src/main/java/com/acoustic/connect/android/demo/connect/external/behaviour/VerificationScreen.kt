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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.acoustic.connect.android.connectmod.composeui.customcomposable.LoggedText
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
import com.acoustic.connect.android.demo.connect.external.ui.components.contractTag
import com.acoustic.connect.android.demo.connect.external.ui.theme.BrandGreen
import com.acoustic.connect.android.demo.connect.external.ui.theme.DarkGrey
import com.acoustic.connect.android.demo.connect.external.ui.theme.Violet
import com.acoustic.connect.android.demo.connect.external.shared.R as SharedR

/**
 * The release-verification surface for the fixes in [Scenarios]. Reached from the Behaviour hub.
 *
 * Each card states what to do and what a fixed build produces; one carries a "baseline only"
 * banner, because its fix has no published artifact yet and a quiet run there is not a pass. The
 * order follows the React Native and iOS samples. Cards whose body doubles as a general demo —
 * custom event, signal, capture control, the modals — share that body with the Showcase; only the
 * scenario frame belongs to this screen.
 */
@Composable
fun VerificationScreen(onNavigate: (String) -> Unit) {
    ScreenviewUnloadEffect(ScreenName.VERIFICATION)

    DemoScreen {
        LogoHeader(title = stringResource(SharedR.string.verification_header))

        DemoCard(title = stringResource(SharedR.string.verification_intro_title)) {
            BodyText(stringResource(SharedR.string.verification_intro_body))
            BodyText(stringResource(SharedR.string.verification_intro_baseline))
        }

        ScenarioCard(Scenarios.SCREENVIEW_REFERRER) {
            SecondaryButton(
                title = stringResource(SharedR.string.verification_open_screen_views),
                tag = SampleId.BTN_OPEN_SCREEN_VIEWS,
                onClick = { onNavigate(ScreenName.SCREEN_VIEWS) },
            )
        }

        ScenarioCard(Scenarios.CUSTOM_EVENT_VALUE_TYPES) { CustomEventBody() }

        SignalCard()

        IdentityDefaultsCard()

        MaskedFieldCard()

        AccessibilityMaskCard()

        MaskingRulesCard()

        CaptureControlCard()

        ScenarioCard(Scenarios.WEBVIEW_POST_NOT_REPLAYED_AS_GET) {
            SecondaryButton(
                title = stringResource(SharedR.string.verification_open_webview),
                tag = SampleId.BTN_OPEN_WEBVIEW_POST,
                onClick = { onNavigate(ScreenName.WEBVIEW_POST) },
            )
        }

        ScenarioCard(Scenarios.REPLAY_CAPTURES_MODAL) {
            BodyText(stringResource(SharedR.string.verification_modal_body))
        }

        ReplayModalCard(transparent = false)

        ReplayModalCard(transparent = true)

        ScenarioCard(Scenarios.ANDROID_COMPILE_CLASSPATH) {
            NoteBox(accent = BrandGreen) {
                LoggedText(
                    text = stringResource(SharedR.string.verification_classpath_note),
                    color = Violet,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                )
            }
        }
    }
}

/**
 * Exercises the SDK's own identity defaults. The Identity tab always passes an explicit signal
 * type and parameters, so it never reaches the defaulting path; the first button omits both, the
 * second is the explicit contrast case.
 */
@Composable
private fun IdentityDefaultsCard() {
    var result by rememberSaveable { mutableStateOf<String?>(null) }
    ScenarioCard(Scenarios.IDENTITY_LOGIN_METHOD_DEFAULT) {
        PrimaryButton(
            title = stringResource(SharedR.string.verification_identity_defaulted),
            tag = SampleId.BTN_IDENTITY_DEFAULTED,
            onClick = { result = VerificationActions.logIdentityDefaulted() },
        )
        SecondaryButton(
            title = stringResource(SharedR.string.verification_identity_explicit),
            tag = SampleId.BTN_IDENTITY_EXPLICIT,
            onClick = { result = VerificationActions.logIdentityExplicit() },
        )
        result?.let { MonoText(it, tag = SampleId.TXT_IDENTITY_DEFAULTS_RESULT) }
    }
}

/**
 * Masking is the observable proxy for the layout config reaching the SDK: if the rules are applied
 * the typed value arrives masked, and if they are silently dropped it arrives verbatim.
 */
@Composable
private fun MaskedFieldCard() {
    var value by rememberSaveable { mutableStateOf("") }
    val label = stringResource(SharedR.string.verification_masked_label)
    ScenarioCard(Scenarios.LAYOUT_CONFIG_APPLIED) {
        DemoTextField(
            label = label,
            placeholder = stringResource(SharedR.string.showcase_masked_placeholder),
            value = value,
            tag = SampleId.FIELD_MASKED,
            // The Compose capture masks by label; "Masked field" is in MaskAccessibilityLabelList.
            maskLabel = label,
            onValueChange = { value = it },
        )
        NoteBox { HintText(stringResource(SharedR.string.verification_masked_hint)) }
    }
}

/**
 * Checks that masking reaches the captured accessibility object, not just the value. Each row
 * shows the same address: with no content description, with one, and with one plus a state
 * description carrying the address itself.
 */
@Composable
private fun AccessibilityMaskCard() {
    var typed by rememberSaveable { mutableStateOf("") }
    val address = stringResource(SharedR.string.verification_a11y_address)
    val label = stringResource(SharedR.string.verification_a11y_label)
    ScenarioCard(Scenarios.ACCESSIBILITY_LABEL_MASKING) {
        AccessibilityRow(stringResource(SharedR.string.verification_a11y_row_implicit)) {
            AddressText(address, tag = SampleId.A11Y_IMPLICIT)
        }
        AccessibilityRow(stringResource(SharedR.string.verification_a11y_row_explicit)) {
            AddressText(address, tag = SampleId.A11Y_EXPLICIT, contentDescription = label)
        }
        AccessibilityRow(stringResource(SharedR.string.verification_a11y_row_value)) {
            AddressText(address, tag = SampleId.A11Y_VALUE, contentDescription = label, stateDescription = address)
        }
        NoteBox { HintText(stringResource(SharedR.string.verification_a11y_hint)) }
        DemoTextField(
            label = stringResource(SharedR.string.verification_a11y_field),
            placeholder = stringResource(SharedR.string.showcase_masked_placeholder),
            value = typed,
            tag = SampleId.A11Y_FIELD,
            onValueChange = { typed = it },
        )
    }
}

/**
 * Checks that value, id and password rules reach every captured surface, not only the
 * accessibility label: the layout text, generated ids, tap targets and the tap's screenshot.
 */
@Composable
private fun MaskingRulesCard() {
    val address = stringResource(SharedR.string.verification_a11y_address)
    val savedPassword = stringResource(SharedR.string.verification_rules_password)
    // Filled in from the start, so the layout captured when the screen opens carries the values.
    var email by rememberSaveable { mutableStateOf(address) }
    var password by rememberSaveable { mutableStateOf(savedPassword) }
    var typed by rememberSaveable { mutableStateOf("") }
    var sent by rememberSaveable { mutableStateOf(false) }
    ScenarioCard(Scenarios.MASKING_VALUE_ID_RULES) {
        AccessibilityRow(stringResource(SharedR.string.verification_rules_row_labelled)) {
            AddressText(
                address,
                tag = SampleId.MASK_RULES_LABELLED,
                contentDescription = stringResource(SharedR.string.verification_rules_label),
            )
        }
        AccessibilityRow(stringResource(SharedR.string.verification_rules_row_unlabelled)) {
            AddressText(address, tag = SampleId.MASK_RULES_UNLABELLED)
        }
        AccessibilityRow(stringResource(SharedR.string.verification_rules_row_card)) {
            // Compose ids are generated from the label, so MaskIdList matches "Card_number".
            AddressText(
                stringResource(SharedR.string.verification_rules_card),
                tag = SampleId.MASK_RULES_CARD,
                contentDescription = stringResource(SharedR.string.verification_rules_card_label),
            )
        }
        DemoTextField(
            label = stringResource(SharedR.string.verification_rules_email_field),
            placeholder = address,
            value = email,
            tag = SampleId.MASK_RULES_EMAIL_FIELD,
            maskLabel = stringResource(SharedR.string.verification_rules_email_field),
            onValueChange = { email = it },
        )
        DemoTextField(
            label = stringResource(SharedR.string.verification_rules_password_field),
            placeholder = "",
            value = password,
            tag = SampleId.MASK_RULES_PASSWORD,
            maskLabel = stringResource(SharedR.string.verification_rules_password_field),
            visualTransformation = PasswordVisualTransformation(),
            onValueChange = { password = it },
        )
        DemoTextField(
            label = stringResource(SharedR.string.verification_rules_typed_field),
            placeholder = stringResource(SharedR.string.showcase_masked_placeholder),
            value = typed,
            tag = SampleId.MASK_RULES_TYPED,
            maskLabel = stringResource(SharedR.string.verification_rules_typed_field),
            onValueChange = { typed = it },
        )
        PrimaryButton(
            title = stringResource(SharedR.string.verification_rules_send),
            tag = SampleId.BTN_MASK_RULES_SEND,
            onClick = { sent = true },
        )
        if (sent) MonoText(stringResource(SharedR.string.verification_rules_sent), tag = SampleId.TXT_MASK_RULES_RESULT)
        NoteBox { HintText(stringResource(SharedR.string.verification_rules_hint)) }
    }
}

@Composable
private fun AccessibilityRow(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        LoggedText(text = title, color = DarkGrey, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        content()
    }
}

@Composable
private fun AddressText(
    address: String,
    tag: String,
    contentDescription: String? = null,
    stateDescription: String? = null,
) {
    LoggedText(
        text = address,
        color = Violet,
        fontSize = 13.sp,
        fontFamily = FontFamily.Monospace,
        // LoggedText publishes maskLabel as the element's content description.
        maskLabel = contentDescription,
        modifier = Modifier
            .contractTag(tag)
            .then(if (stateDescription != null) Modifier.semantics { this.stateDescription = stateDescription } else Modifier),
    )
}
