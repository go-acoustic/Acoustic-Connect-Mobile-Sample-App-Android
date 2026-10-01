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
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import com.acoustic.connect.android.demo.connect.external.R
import com.acoustic.connect.android.demo.connect.external.contract.ScreenName
import com.acoustic.connect.android.demo.connect.external.ui.ScreenFragment
import com.acoustic.connect.android.demo.connect.external.ui.onClick
import com.acoustic.connect.android.demo.connect.external.ui.showResult
import com.acoustic.connect.android.demo.connect.external.shared.R as SharedR

/**
 * The general-purpose demo of the SDK's behaviour capture, for someone integrating it for the
 * first time. Reached from the Behaviour hub.
 *
 * Ordering follows what an integrator meets first: what the SDK captures with no code (taps,
 * text), then screen views, the explicit logging calls (custom events, signals, exceptions),
 * dialogs and modals, and finally runtime control. The Gestures and App state entries are Android
 * additions; the other cards match the React Native, iOS and Compose samples, and every SDK call
 * goes through [ShowcaseActions] so both Android samples send and show the same thing.
 */
class ShowcaseFragment : ScreenFragment(R.layout.fragment_showcase) {

    override val screenName = ScreenName.SHOWCASE
    override val headerTitle get() = getString(SharedR.string.showcase_header)

    private var taps = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        taps = savedInstanceState?.getInt(STATE_TAPS) ?: 0
        val navController = findNavController()

        view.onClick(R.id.btn_showcase_open_detail) {
            navController.navigate(R.id.showcaseDetailFragment, ShowcaseDetailFragment.args(1))
        }

        val tapsLine = view.findViewById<TextView>(R.id.txt_showcase_taps)
        // The SDK identifies a view by its fully qualified resource name, <applicationId>:id/<name>,
        // so ask for that rather than spelling it out.
        val tapControlId = resources.getResourceName(R.id.btn_showcase_tap)
        fun renderTaps() {
            tapsLine.text = getString(R.string.showcase_taps_result, taps, tapControlId)
        }
        renderTaps()
        view.onClick(R.id.btn_showcase_tap) {
            taps += 1
            renderTaps()
        }

        view.onClick(R.id.btn_open_gestures) { navController.navigate(R.id.gesturesFragment) }
        view.onClick(R.id.btn_open_app_state) { navController.navigate(R.id.appStateFragment) }

        ShowcaseCards.bindCustomEvent(view)
        ShowcaseCards.bindSignal(view)

        view.onClick(R.id.btn_showcase_exception) {
            view.showResult(R.id.txt_showcase_exception_result, ShowcaseActions.logHandledException())
        }

        view.onClick(R.id.btn_showcase_dialog) {
            ShowcaseActions.showDialog(requireActivity()) { outcome ->
                view.showResult(R.id.txt_showcase_dialog_result, outcome)
            }
        }

        ShowcaseCards.bindReplayModals(view, this)
        ShowcaseCards.bindCaptureControl(view, this)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(STATE_TAPS, taps)
    }

    private companion object {
        const val STATE_TAPS = "taps"
    }
}
