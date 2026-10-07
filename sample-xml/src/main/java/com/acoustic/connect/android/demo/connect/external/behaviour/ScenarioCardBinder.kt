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
import com.acoustic.connect.android.demo.connect.external.R

/**
 * Fills the `view_scenario_header` inside [bind]'s card from a [Scenario]: its title, key,
 * channel, the Do / Expect text, and — when the fix has no published artifact yet — the
 * baseline-only banner. That banner is the point of the card: a card whose fix has not shipped
 * still runs and still looks healthy, so without it "no error" reads as a pass when it is really
 * a baseline.
 *
 * Every scenario card includes the same header, so the lookups are scoped to the card passed in.
 */
object ScenarioCardBinder {

    fun bind(card: View, scenario: Scenario) {
        card.text(R.id.scenario_title, scenario.title)
        card.text(R.id.scenario_key, scenario.key)
        card.text(R.id.scenario_channel, "${scenario.channel.label} · ${scenario.platform.label}")
        card.text(R.id.scenario_action, scenario.action)
        card.text(R.id.scenario_expected, scenario.expected)

        val blocked = scenario.blockedBy
        card.findViewById<View>(R.id.scenario_blocked).visibility = if (blocked == null) View.GONE else View.VISIBLE
        card.text(R.id.scenario_blocked_title, Scenarios.BLOCKED_BANNER_TITLE)
        card.text(R.id.scenario_blocked_body, blocked.orEmpty())
    }

    private fun View.text(id: Int, value: String) {
        findViewById<TextView>(id).text = value
    }
}
