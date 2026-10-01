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
import com.acoustic.connect.android.connectmod.Connect
import com.acoustic.connect.android.connectmod.model.ConnectScreenviewType
import com.acoustic.connect.android.demo.connect.external.contract.ScreenName

/**
 * The direct screen-view calls behind the Screen Views and Case screens, and the lines they print.
 *
 * "Direct" means `Connect.logScreenview` with an exact name, bypassing navigation: the only way to
 * send a name no screen can carry. Both samples call these, so the same case sends the same
 * message and prints the same line in Compose and in XML. The result reports whether the SDK
 * accepted the message for its queue, never whether the collector received it.
 */
object ScreenViewActions {

    /** The referrer the exact-name card sends, as in the React Native and iOS samples. */
    const val DIRECT_REFERRER = "Screen View Diagnostics"

    /** How many lines the exact-name card keeps, newest first. */
    const val LOG_LIMIT = 12

    /**
     * Logs a LOAD for [name]. Only the referrer-less overload takes a null name, so the null case
     * goes through it and carries whatever referrer the SDK tracks rather than [referrer].
     */
    fun logDirect(activity: Activity, name: String?, referrer: String): Boolean =
        if (name == null) {
            Connect.logScreenview(activity, null, ConnectScreenviewType.LOAD)
        } else {
            Connect.logScreenview(activity, name, ConnectScreenviewType.LOAD, referrer)
        }

    /** Sends [case] from the exact-name card and returns its line, e.g. `✓ catalog → Catalog`. */
    fun sendDirect(activity: Activity, case: ScreenViewCase): String =
        logLine(case, logDirect(activity, case.name, DIRECT_REFERRER))

    fun logLine(case: ScreenViewCase, queued: Boolean): String = "${mark(queued)} ${case.id} → ${describeName(case.name)}"

    /** [log] with [line] on top, trimmed to [LOG_LIMIT]. */
    fun prepend(log: List<String>, line: String): List<String> = (listOf(line) + log).take(LOG_LIMIT)

    /**
     * Re-sends a case screen's name directly, with Screen Views as the referrer, so the
     * navigation-driven message can be compared against an explicit one.
     */
    fun relog(activity: Activity, case: ScreenViewCase): String =
        "${mark(logDirect(activity, case.name, ScreenName.SCREEN_VIEWS))} re-logged directly"

    private fun mark(ok: Boolean) = if (ok) "✓" else "✗"
}
