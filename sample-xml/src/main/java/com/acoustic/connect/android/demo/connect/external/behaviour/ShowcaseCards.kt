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

import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.acoustic.connect.android.demo.connect.external.R
import com.acoustic.connect.android.demo.connect.external.ui.onClick
import com.acoustic.connect.android.demo.connect.external.ui.showResult

/**
 * Binds the cards the Showcase and Verification screens share — the custom-event body, the signal
 * and capture-control cards, and the two replay modals. Each screen includes the same layouts, so
 * a card sends the same payload and shows the same result under the same ids on both.
 */
object ShowcaseCards {

    /** `view_custom_event_body`. */
    fun bindCustomEvent(root: View) {
        root.findViewById<TextView>(R.id.custom_event_payload).text = ShowcaseActions.CUSTOM_EVENT_PAYLOAD_TEXT
        root.onClick(R.id.btn_send_custom_event) {
            root.showResult(R.id.txt_custom_event_result, ShowcaseActions.sendCustomEvent())
        }
    }

    /** `view_signal_card`. */
    fun bindSignal(root: View) {
        fun show(text: String) {
            root.findViewById<View>(R.id.signal_result_box).visibility = View.VISIBLE
            root.showResult(R.id.txt_signal_result, text)
        }
        root.onClick(R.id.btn_send_nested_signal) { show(ShowcaseActions.sendNestedSignal()) }
        root.onClick(R.id.btn_send_flat_signal) { show(ShowcaseActions.sendFlatSignal()) }
    }

    /** `view_capture_card`. */
    fun bindCaptureControl(root: View, fragment: Fragment) {
        root.onClick(R.id.btn_capture_disable) {
            root.showResult(R.id.txt_capture_state, ShowcaseActions.disableSdk())
        }
        root.onClick(R.id.btn_capture_enable) {
            root.showResult(R.id.txt_capture_state, ShowcaseActions.enableSdk(fragment.requireActivity()))
        }
    }

    /** `view_replay_modal_cards`. */
    fun bindReplayModals(root: View, fragment: Fragment) {
        root.onClick(R.id.btn_open_replay_modal_opaque) {
            ReplayModal.show(fragment.requireActivity(), transparent = false)
        }
        root.onClick(R.id.btn_open_replay_modal_transparent) {
            ReplayModal.show(fragment.requireActivity(), transparent = true)
        }
    }
}
