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

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.acoustic.connect.android.connectmod.composeui.customcomposable.LoggedText
import com.acoustic.connect.android.demo.connect.external.analytics.ScreenviewUnloadEffect
import com.acoustic.connect.android.demo.connect.external.contract.SampleId
import com.acoustic.connect.android.demo.connect.external.contract.ScreenName
import com.acoustic.connect.android.demo.connect.external.ui.components.BodyText
import com.acoustic.connect.android.demo.connect.external.ui.components.DemoCard
import com.acoustic.connect.android.demo.connect.external.ui.components.DemoScreen
import com.acoustic.connect.android.demo.connect.external.ui.components.LogoHeader
import com.acoustic.connect.android.demo.connect.external.ui.components.PrimaryButton
import com.acoustic.connect.android.demo.connect.external.ui.theme.DarkGrey
import com.acoustic.connect.android.demo.connect.external.shared.R as SharedR

/**
 * Behaviour tab root — the entry point into the analytics half of the SDK. The Showcase is the
 * general-purpose demo: one card per capture feature, written for someone integrating the SDK for
 * the first time. It lives in a stack because several cards need somewhere to navigate to: a
 * screen view is only logged on a real navigation.
 */
@Composable
fun BehaviourScreen(onOpenShowcase: () -> Unit) {
    ScreenviewUnloadEffect(ScreenName.BEHAVIOUR)

    DemoScreen {
        LogoHeader(title = stringResource(SharedR.string.behaviour_header))

        DemoCard(title = stringResource(SharedR.string.behaviour_showcase_title)) {
            BodyText(stringResource(SharedR.string.behaviour_showcase_body))
            PrimaryButton(
                title = stringResource(SharedR.string.behaviour_open_showcase),
                tag = SampleId.BTN_OPEN_SHOWCASE,
                onClick = onOpenShowcase,
            )
        }

        LoggedText(
            text = stringResource(SharedR.string.behaviour_footnote),
            color = DarkGrey,
            fontSize = 12.sp,
            lineHeight = 18.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        )
    }
}
