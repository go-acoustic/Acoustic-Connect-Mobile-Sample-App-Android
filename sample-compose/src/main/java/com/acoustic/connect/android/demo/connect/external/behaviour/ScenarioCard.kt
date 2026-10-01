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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.acoustic.connect.android.connectmod.composeui.customcomposable.LoggedText
import com.acoustic.connect.android.demo.connect.external.ui.components.BodyText
import com.acoustic.connect.android.demo.connect.external.ui.components.DemoCard
import com.acoustic.connect.android.demo.connect.external.ui.components.NoteBox
import com.acoustic.connect.android.demo.connect.external.ui.theme.DarkGrey
import com.acoustic.connect.android.demo.connect.external.ui.theme.Periwinkle
import com.acoustic.connect.android.demo.connect.external.ui.theme.Violet
import com.acoustic.connect.android.demo.connect.external.shared.R as SharedR

/**
 * Card shell for one fix under verification. Prints what to do and what a fixed build produces, so
 * the tester does not have to hold the expectation in their head — and, when the fix has no
 * published native artifact yet, says so in a banner.
 *
 * That banner is the point of this component. A card whose fix has not shipped still runs and
 * still looks healthy; without the warning, "no error" reads as a pass when it is really a
 * baseline.
 */
@Composable
fun ScenarioCard(
    scenario: Scenario,
    content: @Composable ColumnScope.() -> Unit = {},
) {
    DemoCard(title = scenario.title) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LoggedText(
                text = scenario.key,
                color = Periwinkle,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
            )
            LoggedText(
                text = "${scenario.channel.label} · ${scenario.platform.label}",
                color = DarkGrey,
                fontSize = 10.sp,
            )
        }

        scenario.blockedBy?.let { blockedBy ->
            NoteBox(accent = Periwinkle) {
                LoggedText(
                    text = Scenarios.BLOCKED_BANNER_TITLE,
                    color = Violet,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                )
                LoggedText(text = blockedBy, color = Violet, fontSize = 12.sp, lineHeight = 18.sp)
            }
        }

        ScenarioLabel(stringResource(SharedR.string.scenario_do))
        BodyText(scenario.action)
        ScenarioLabel(stringResource(SharedR.string.scenario_expect))
        BodyText(scenario.expected)

        content()
    }
}

@Composable
private fun ScenarioLabel(text: String) {
    LoggedText(
        text = text.uppercase(),
        color = DarkGrey,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
    )
}
