/*
 * Copyright (C) 2026 Acoustic, L.P. All rights reserved.
 *
 * Licensed under the Acoustic License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy
 * at https://www.acoustic.com/licenses/acoustic-license
 *
 * Sample app provided "as is", without warranty of any kind.
 */
package com.acoustic.connect.android.demo.connect.external.ui

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import com.acoustic.connect.android.connectmod.Connect
import com.acoustic.connect.android.connectmod.model.ConnectScreenviewType
import com.acoustic.connect.android.demo.connect.external.R
import com.acoustic.connect.android.demo.connect.external.analytics.ScreenViewTrail
import com.acoustic.connect.android.demo.connect.external.analytics.SignalLog

/**
 * A screen that logs its own screen view, which is how a Views app names its screens.
 *
 * LOAD is logged when the view is created and UNLOAD when it is destroyed, so each visit is one
 * matched pair: the view survives backgrounding, so logging on resume would repeat the LOAD with
 * no UNLOAD in between. Going back to a screen recreates its view, so returning logs it again with
 * the screen just left as the referrer — the same chain the Compose sample gets from the SDK.
 *
 * Both return values are recorded in the [SignalLog], because whether the SDK accepted a screen
 * view is exactly what the App state screen exists to show.
 */
abstract class ScreenFragment(@LayoutRes layout: Int) : Fragment(layout) {

    /** The logical page name this screen logs — one of the shared contract's names. */
    protected abstract val screenName: String

    /** The title under the logo, for screens that include `view_logo_header`. */
    protected open val headerTitle: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        headerTitle?.let { view.findViewById<TextView>(R.id.header_title)?.text = it }

        val activity = requireActivity()
        val referrer = ScreenViewTrail.shared.enter(screenName)
        Connect.logScreenLayout(activity, screenName)
        SignalLog.record(
            "screenviewLoad",
            screenName,
            Connect.logScreenview(activity, screenName, ConnectScreenviewType.LOAD, referrer),
        )
    }

    override fun onDestroyView() {
        SignalLog.record(
            "screenviewUnload",
            screenName,
            Connect.logScreenview(requireActivity(), screenName, ConnectScreenviewType.UNLOAD),
        )
        super.onDestroyView()
    }
}
