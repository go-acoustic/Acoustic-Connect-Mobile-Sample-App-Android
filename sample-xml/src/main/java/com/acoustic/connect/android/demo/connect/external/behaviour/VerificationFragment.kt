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

import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.navigation.fragment.findNavController
import com.acoustic.connect.android.demo.connect.external.R
import com.acoustic.connect.android.demo.connect.external.contract.ScreenName
import com.acoustic.connect.android.demo.connect.external.ui.ScreenFragment
import com.acoustic.connect.android.demo.connect.external.ui.onClick
import com.acoustic.connect.android.demo.connect.external.ui.showResult
import com.acoustic.connect.android.demo.connect.external.shared.R as SharedR

/**
 * The release-verification surface for the fixes in [Scenarios]. Reached from the Behaviour hub.
 *
 * Each card states what to do and what a fixed build produces; one carries a "baseline only"
 * banner, because its fix has no published artifact yet and a quiet run there is not a pass. The
 * order follows the React Native and iOS samples. Cards whose body doubles as a general demo —
 * custom event, signal, capture control, the modals — are the Showcase's own layouts, bound by
 * [ShowcaseCards]; only the scenario frame belongs to this screen.
 */
class VerificationFragment : ScreenFragment(R.layout.fragment_verification) {

    override val screenName = ScreenName.VERIFICATION
    override val headerTitle get() = getString(SharedR.string.verification_header)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listOf(
            R.id.card_screenview_referrer to Scenarios.SCREENVIEW_REFERRER,
            R.id.card_custom_event to Scenarios.CUSTOM_EVENT_VALUE_TYPES,
            R.id.card_identity_defaults to Scenarios.IDENTITY_LOGIN_METHOD_DEFAULT,
            R.id.card_layout_config to Scenarios.LAYOUT_CONFIG_APPLIED,
            R.id.card_accessibility to Scenarios.ACCESSIBILITY_LABEL_MASKING,
            R.id.card_masking_rules to Scenarios.MASKING_VALUE_ID_RULES,
            R.id.card_webview_post to Scenarios.WEBVIEW_POST_NOT_REPLAYED_AS_GET,
            R.id.card_replay_modal to Scenarios.REPLAY_CAPTURES_MODAL,
            R.id.card_compile_classpath to Scenarios.ANDROID_COMPILE_CLASSPATH,
        ).forEach { (card, scenario) -> ScenarioCardBinder.bind(view.findViewById(card), scenario) }

        ShowcaseCards.bindCustomEvent(view)
        ShowcaseCards.bindSignal(view)
        ShowcaseCards.bindCaptureControl(view, this)
        ShowcaseCards.bindReplayModals(view, this)

        // The Identity tab always passes an explicit signal type and parameters, so it never
        // reaches the SDK's defaulting path; the first button omits both, the second is the
        // explicit contrast case.
        view.onClick(R.id.btn_identity_defaulted) {
            view.showResult(R.id.txt_identity_defaults_result, VerificationActions.logIdentityDefaulted())
        }
        view.onClick(R.id.btn_identity_explicit) {
            view.showResult(R.id.txt_identity_defaults_result, VerificationActions.logIdentityExplicit())
        }

        // The third row also carries the address as its state description, the value an
        // accessibility service reads alongside the label.
        ViewCompat.setStateDescription(
            view.findViewById(R.id.a11y_value),
            getString(SharedR.string.verification_a11y_address),
        )

        // The tap is the capture: its screenshot is what shows whether typed text is obscured.
        view.onClick(R.id.btn_mask_rules_send) {
            view.showResult(R.id.txt_mask_rules_result, getString(SharedR.string.verification_rules_sent))
        }

        view.onClick(R.id.btn_open_screen_views) { findNavController().navigate(R.id.screenViewsFragment) }
        view.onClick(R.id.btn_open_webview_post) { findNavController().navigate(R.id.webViewPostFragment) }
    }
}
