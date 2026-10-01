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
import android.widget.Button
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.acoustic.connect.android.demo.connect.external.R
import com.acoustic.connect.android.demo.connect.external.ui.ScreenFragment
import com.acoustic.connect.android.demo.connect.external.ui.onClick
import com.acoustic.connect.android.demo.connect.external.ui.showResult
import com.acoustic.connect.android.demo.connect.external.shared.R as SharedR

/**
 * The screen for one screen-name case. Arriving here is the event under test: [ScreenFragment]
 * has just logged a screen view under the case's name, so a type-2 message with that exact name
 * is on its way to the collector.
 *
 * The screen prints the name it was entered with, so a tester can match what the app claims to
 * have logged against what the collector received and what the inferred signal's `url` ends up
 * being. Pushing a further case builds a deeper stack, which checks that the top screen's name is
 * what gets logged rather than a joined path. Mirrors the React Native sample's Case screen.
 */
class ScreenViewCaseFragment : ScreenFragment(R.layout.fragment_screen_view_case) {

    private val case: ScreenViewCase
        get() = requireNotNull(ScreenViewCases.byId(requireArguments().getString(ARG_CASE_ID).orEmpty())) {
            "unknown screen-view case"
        }

    override val screenName: String get() = case.name.orEmpty()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val case = case
        view.findViewById<TextView>(R.id.case_name).text = describeName(case.name)
        view.findViewById<TextView>(R.id.case_meta).text = getString(SharedR.string.case_meta, screenName.length)
        view.findViewById<TextView>(R.id.case_probes).text = case.probes

        view.onClick(R.id.btn_case_relog) {
            view.showResult(R.id.txt_case_relog_result, ScreenViewActions.relog(requireActivity(), case))
        }

        ScreenViewCases.next(case)?.let { next ->
            view.findViewById<Button>(R.id.btn_case_push_next).apply {
                text = getString(SharedR.string.case_push_next, next.label)
                visibility = View.VISIBLE
                setOnClickListener { findNavController().navigate(R.id.screenViewCaseFragment, args(next)) }
            }
        }
    }

    companion object {
        private const val ARG_NAME = "name"
        private const val ARG_CASE_ID = "caseId"

        /** Arguments for [case]'s screen; the name is also the toolbar title. */
        fun args(case: ScreenViewCase): Bundle = bundleOf(ARG_NAME to case.name.orEmpty(), ARG_CASE_ID to case.id)
    }
}
