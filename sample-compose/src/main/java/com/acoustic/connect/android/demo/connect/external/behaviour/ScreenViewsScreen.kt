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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.acoustic.connect.android.demo.connect.external.R
import com.acoustic.connect.android.demo.connect.external.analytics.ScreenviewUnloadEffect
import com.acoustic.connect.android.demo.connect.external.contract.SampleId
import com.acoustic.connect.android.demo.connect.external.contract.ScreenName
import com.acoustic.connect.android.demo.connect.external.ui.components.BodyText
import com.acoustic.connect.android.demo.connect.external.ui.components.CaptionText
import com.acoustic.connect.android.demo.connect.external.ui.components.DemoCard
import com.acoustic.connect.android.demo.connect.external.ui.components.DemoScreen
import com.acoustic.connect.android.demo.connect.external.ui.components.HintText
import com.acoustic.connect.android.demo.connect.external.ui.components.LogoHeader
import com.acoustic.connect.android.demo.connect.external.ui.components.MonoText
import com.acoustic.connect.android.demo.connect.external.ui.components.NoteBox
import com.acoustic.connect.android.demo.connect.external.ui.components.PrimaryButton
import com.acoustic.connect.android.demo.connect.external.ui.components.SecondaryButton
import com.acoustic.connect.android.demo.connect.external.ui.theme.Periwinkle
import com.acoustic.connect.android.demo.connect.external.shared.R as SharedR

/**
 * Whether [ScreenViewCase.name] can be a navigation route. The SDK's Compose integration logs a
 * destination's route as its screen name, so a case screen's route has to be the name itself —
 * and Navigation rejects a blank route, which leaves the whitespace case to the exact-name card.
 */
val ScreenViewCase.isRoutable: Boolean get() = !name.isNullOrBlank()

/** The navigation cases that get a destination of their own. */
val routableScreenViewCases: List<ScreenViewCase> = ScreenViewCases.NAV.filter { it.isRoutable }

/**
 * The verification surface for the `pageView` signal the platform infers from every screen view.
 * Reached from the Verification screen's `screenview-referrer` card.
 *
 * A mobile screen view carries no URL, so the server-side rule falls back to the screen's name
 * for the inferred signal's `url`; each case below is one test of that fallback. The Navigate
 * card opens a destination routed by the case's name, so the name reaches the SDK the way a
 * Compose app sends it. Mirrors the React Native sample's Screen Views screen.
 */
@Composable
fun ScreenViewsScreen(onOpenCase: (ScreenViewCase) -> Unit) {
    ScreenviewUnloadEffect(ScreenName.SCREEN_VIEWS)

    DemoScreen {
        LogoHeader(title = stringResource(SharedR.string.screen_views_header))

        DemoCard(title = stringResource(SharedR.string.screen_views_how_title)) {
            BodyText(stringResource(SharedR.string.screen_views_how_body))
            BodyText(stringResource(SharedR.string.screen_views_how_drive))
            NoteBox(accent = Periwinkle) { HintText(stringResource(SharedR.string.screen_views_warn)) }
        }

        DemoCard(title = stringResource(SharedR.string.screen_views_navigate_title)) {
            BodyText(stringResource(R.string.screen_views_navigate_body))
            BodyText(stringResource(SharedR.string.screen_views_repeat_note))
            ScreenViewCases.NAV.forEach { case ->
                CaseBlock {
                    SecondaryButton(
                        title = case.label,
                        tag = SampleId.navigateButton(case.id),
                        enabled = case.isRoutable,
                        onClick = { onOpenCase(case) },
                    )
                    MonoText(stringResource(SharedR.string.screen_views_logs, describeName(case.name)))
                    if (!case.isRoutable) CaptionText(stringResource(R.string.screen_views_blank_route))
                }
            }
        }

        DirectScreenViewCard()
    }
}

/** Sends each direct case with its exact name, bypassing navigation. */
@Composable
private fun DirectScreenViewCard() {
    val activity = LocalContext.current as? Activity
    var log by rememberSaveable { mutableStateOf(emptyList<String>()) }
    fun send(case: ScreenViewCase) {
        activity?.let { log = ScreenViewActions.prepend(log, ScreenViewActions.sendDirect(it, case)) }
    }

    DemoCard(title = stringResource(SharedR.string.screen_views_direct_title)) {
        BodyText(stringResource(SharedR.string.screen_views_direct_body))
        NoteBox { HintText(stringResource(SharedR.string.screen_views_direct_android)) }
        PrimaryButton(
            title = stringResource(SharedR.string.screen_views_send_all, ScreenViewCases.DIRECT.size),
            tag = SampleId.BTN_SCREENVIEW_SEND_ALL,
            onClick = { ScreenViewCases.DIRECT.forEach(::send) },
        )
        ScreenViewCases.DIRECT.forEach { case ->
            CaseBlock {
                SecondaryButton(title = case.label, tag = SampleId.screenViewButton(case.id), onClick = { send(case) })
                CaptionText(case.probes)
            }
        }
        if (log.isNotEmpty()) {
            NoteBox {
                MonoText(log.first(), tag = SampleId.TXT_SCREENVIEW_RESULT)
                log.drop(1).forEach { MonoText(it) }
            }
        }
    }
}

@Composable
private fun CaseBlock(content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) { content() }
}
