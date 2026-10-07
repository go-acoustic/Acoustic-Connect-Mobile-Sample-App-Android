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
import com.acoustic.connect.android.demo.connect.external.contract.ScreenName
import com.acoustic.connect.android.demo.connect.external.ui.ScreenFragment
import com.acoustic.connect.android.demo.connect.external.shared.R as SharedR

/**
 * The screen the Showcase's "Screen views" card opens. Arriving here is the demo: this screen has
 * just logged a screen view under its own name, with the previous screen as referrer.
 *
 * "Push another" stacks the next depth, so the referrer chain can be seen to advance and the leaf
 * screen's name is what gets logged rather than a joined path. The chain stops at
 * [ScreenName.MAX_SHOWCASE_DEPTH].
 */
class ShowcaseDetailFragment : ScreenFragment(R.layout.fragment_showcase_detail) {

    private val depth: Int get() = requireArguments().getInt(ARG_DEPTH, 1)

    override val screenName: String get() = ScreenName.showcaseDetail(depth)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val atCap = depth >= ScreenName.MAX_SHOWCASE_DEPTH

        view.findViewById<TextView>(R.id.detail_name).text = screenName
        view.findViewById<TextView>(R.id.detail_depth).text =
            getString(SharedR.string.detail_chain_body, depth, ScreenName.MAX_SHOWCASE_DEPTH)

        view.findViewById<Button>(R.id.btn_showcase_push_detail).apply {
            isEnabled = !atCap
            setText(if (atCap) SharedR.string.detail_chain_limit else SharedR.string.detail_push_another)
            setOnClickListener { if (!atCap) findNavController().navigate(R.id.showcaseDetailFragment, args(depth + 1)) }
        }
        view.findViewById<View>(R.id.btn_showcase_back).setOnClickListener { findNavController().popBackStack() }
    }

    companion object {
        private const val ARG_NAME = "name"
        private const val ARG_DEPTH = "depth"

        /** Arguments for the detail screen at [depth]; the name is also the toolbar title. */
        fun args(depth: Int): Bundle = bundleOf(ARG_NAME to ScreenName.showcaseDetail(depth), ARG_DEPTH to depth)
    }
}
