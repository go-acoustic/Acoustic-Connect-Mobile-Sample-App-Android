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
import androidx.navigation.fragment.findNavController
import com.acoustic.connect.android.demo.connect.external.R
import com.acoustic.connect.android.demo.connect.external.contract.ScreenName
import com.acoustic.connect.android.demo.connect.external.ui.ScreenFragment
import com.acoustic.connect.android.demo.connect.external.shared.R as SharedR

/**
 * Behaviour tab root — a hub with two entry points into the analytics half of the SDK. The
 * Showcase is the general-purpose demo: one card per capture feature, written for someone
 * integrating the SDK for the first time. Verification is the release-verification surface: one
 * card per shipped fix, each stating what to do and what a fixed build produces. Both live in a
 * stack because several cards need somewhere to navigate to: a screen view is only logged when a
 * screen opens.
 */
class BehaviourFragment : ScreenFragment(R.layout.fragment_behaviour) {

    override val screenName = ScreenName.BEHAVIOUR
    override val headerTitle get() = getString(SharedR.string.behaviour_header)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<View>(R.id.btn_open_showcase).setOnClickListener {
            findNavController().navigate(R.id.showcaseFragment)
        }
        view.findViewById<View>(R.id.btn_open_verification).setOnClickListener {
            findNavController().navigate(R.id.verificationFragment)
        }
    }
}
