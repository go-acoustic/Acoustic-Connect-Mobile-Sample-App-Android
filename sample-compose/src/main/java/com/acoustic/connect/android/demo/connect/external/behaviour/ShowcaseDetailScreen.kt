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

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.acoustic.connect.android.demo.connect.external.analytics.ScreenviewUnloadEffect
import com.acoustic.connect.android.demo.connect.external.contract.SampleId
import com.acoustic.connect.android.demo.connect.external.contract.ScreenName
import com.acoustic.connect.android.demo.connect.external.ui.components.BodyText
import com.acoustic.connect.android.demo.connect.external.ui.components.DemoCard
import com.acoustic.connect.android.demo.connect.external.ui.components.DemoScreen
import com.acoustic.connect.android.demo.connect.external.ui.components.MonoText
import com.acoustic.connect.android.demo.connect.external.ui.components.PrimaryButton
import com.acoustic.connect.android.demo.connect.external.ui.components.SecondaryButton
import com.acoustic.connect.android.demo.connect.external.shared.R as SharedR

/**
 * The screen the Showcase's "Screen views" card navigates to. Arriving here is the demo: the SDK
 * has just logged a screen view named after this route, with the previous screen as referrer.
 * Nothing on this screen calls the SDK to log it.
 *
 * "Push another" stacks the next depth, so the referrer chain can be seen to advance and the leaf
 * screen's name is what gets logged rather than a joined path. The chain stops at
 * [ScreenName.MAX_SHOWCASE_DEPTH].
 */
@Composable
fun ShowcaseDetailScreen(depth: Int, onPushNext: () -> Unit, onBack: () -> Unit) {
    val name = ScreenName.showcaseDetail(depth)
    val atCap = depth >= ScreenName.MAX_SHOWCASE_DEPTH

    ScreenviewUnloadEffect(name)

    DemoScreen {
        DemoCard(title = stringResource(SharedR.string.detail_logged_title)) {
            BodyText(stringResource(SharedR.string.detail_logged_body))
            MonoText(name)
            BodyText(stringResource(SharedR.string.detail_logged_referrer))
        }

        DemoCard(title = stringResource(SharedR.string.detail_chain_title)) {
            BodyText(stringResource(SharedR.string.detail_chain_body, depth, ScreenName.MAX_SHOWCASE_DEPTH))
            PrimaryButton(
                title = stringResource(if (atCap) SharedR.string.detail_chain_limit else SharedR.string.detail_push_another),
                tag = SampleId.BTN_SHOWCASE_PUSH_DETAIL,
                enabled = !atCap,
                onClick = { if (!atCap) onPushNext() },
            )
            SecondaryButton(
                title = stringResource(SharedR.string.detail_back),
                tag = SampleId.BTN_SHOWCASE_BACK,
                onClick = onBack,
            )
        }
    }
}
