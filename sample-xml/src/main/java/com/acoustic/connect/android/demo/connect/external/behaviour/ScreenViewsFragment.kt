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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import com.acoustic.connect.android.demo.connect.external.R
import com.acoustic.connect.android.demo.connect.external.contract.ScreenName
import com.acoustic.connect.android.demo.connect.external.ui.ScreenFragment
import com.acoustic.connect.android.demo.connect.external.shared.R as SharedR

/**
 * The verification surface for the `pageView` signal the platform infers from every screen view.
 * Reached from the Verification screen's `screenview-referrer` card.
 *
 * A mobile screen view carries no URL, so the server-side rule falls back to the screen's name
 * for the inferred signal's `url`; each case below is one test of that fallback. The Navigate
 * card opens a screen that logs the case's name as it appears, the way a Views app names its
 * screens. Mirrors the React Native sample's Screen Views screen.
 */
class ScreenViewsFragment : ScreenFragment(R.layout.fragment_screen_views) {

    override val screenName = ScreenName.SCREEN_VIEWS
    override val headerTitle get() = getString(SharedR.string.screen_views_header)

    private var log: List<String> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        log = savedInstanceState?.getStringArrayList(STATE_LOG).orEmpty()
        val inflater = LayoutInflater.from(view.context)

        val navigateList = view.findViewById<ViewGroup>(R.id.screen_views_navigate_list)
        ScreenViewCases.NAV.forEach { case ->
            navigateList.addCase(inflater, ScreenViewButtons.navigate.getValue(case.id), case.label,
                getString(SharedR.string.screen_views_logs, describeName(case.name)), mono = true) {
                findNavController().navigate(R.id.screenViewCaseFragment, ScreenViewCaseFragment.args(case))
            }
        }

        val directList = view.findViewById<ViewGroup>(R.id.screen_views_direct_list)
        ScreenViewCases.DIRECT.forEach { case ->
            directList.addCase(inflater, ScreenViewButtons.direct.getValue(case.id), case.label, case.probes, mono = false) {
                send(view, case)
            }
        }
        inflater.inflate(R.layout.view_screen_view_log, directList, true)
        view.findViewById<Button>(R.id.btn_screenview_send_all).apply {
            text = getString(SharedR.string.screen_views_send_all, ScreenViewCases.DIRECT.size)
            setOnClickListener { ScreenViewCases.DIRECT.forEach { send(view, it) } }
        }
        renderLog(view)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putStringArrayList(STATE_LOG, ArrayList(log))
    }

    private fun send(view: View, case: ScreenViewCase) {
        log = ScreenViewActions.prepend(log, ScreenViewActions.sendDirect(requireActivity(), case))
        renderLog(view)
    }

    private fun renderLog(view: View) {
        view.findViewById<View>(R.id.screen_views_log).visibility = if (log.isEmpty()) View.GONE else View.VISIBLE
        view.findViewById<TextView>(R.id.txt_screenview_result).text = log.firstOrNull().orEmpty()
        view.findViewById<TextView>(R.id.txt_screenview_history).apply {
            text = log.drop(1).joinToString("\n")
            visibility = if (log.size > 1) View.VISIBLE else View.GONE
        }
    }

    private fun ViewGroup.addCase(
        inflater: LayoutInflater,
        buttonId: Int,
        label: String,
        caption: String,
        mono: Boolean,
        onClick: () -> Unit,
    ) {
        val block = inflater.inflate(R.layout.item_screen_view_case, this, false)
        block.findViewById<Button>(R.id.case_button).apply {
            id = buttonId
            text = label
            setOnClickListener { onClick() }
        }
        block.findViewById<TextView>(R.id.case_caption).apply {
            text = caption
            if (mono) setTextAppearance(R.style.Demo_Text_Mono)
        }
        addView(block)
    }

    private companion object {
        const val STATE_LOG = "log"
    }
}
