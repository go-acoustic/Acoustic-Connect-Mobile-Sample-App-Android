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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.acoustic.connect.android.demo.connect.external.analytics.ScreenviewUnloadEffect
import com.acoustic.connect.android.demo.connect.external.contract.SampleId
import com.acoustic.connect.android.demo.connect.external.ui.components.BodyText
import com.acoustic.connect.android.demo.connect.external.ui.components.CaptionText
import com.acoustic.connect.android.demo.connect.external.ui.components.DemoCard
import com.acoustic.connect.android.demo.connect.external.ui.components.DemoScreen
import com.acoustic.connect.android.demo.connect.external.ui.components.MonoText
import com.acoustic.connect.android.demo.connect.external.ui.components.PrimaryButton
import com.acoustic.connect.android.demo.connect.external.ui.components.SecondaryButton
import com.acoustic.connect.android.demo.connect.external.shared.R as SharedR

/**
 * The screen for one screen-name case. Arriving here is the event under test: the SDK has just
 * logged a screen view named after this destination's route, which is the case's name, so a
 * type-2 message with that exact name is on its way to the collector.
 *
 * The screen prints the name it was entered with, so a tester can match what the app claims to
 * have logged against what the collector received and what the inferred signal's `url` ends up
 * being. Pushing a further case builds a deeper stack, which checks that the top screen's name is
 * what gets logged rather than a joined path. Mirrors the React Native sample's Case screen.
 */
@Composable
fun ScreenViewCaseScreen(case: ScreenViewCase, onPush: (ScreenViewCase) -> Unit) {
    val activity = LocalContext.current as? Activity
    var relogged by rememberSaveable { mutableStateOf<String?>(null) }
    val name = case.name.orEmpty()

    ScreenviewUnloadEffect(name)

    DemoScreen {
        DemoCard(title = stringResource(SharedR.string.case_logged_title)) {
            MonoText(describeName(case.name))
            CaptionText(stringResource(SharedR.string.case_meta, name.length))
            BodyText(case.probes)
        }

        DemoCard(title = stringResource(SharedR.string.case_expected_title)) {
            BodyText(stringResource(SharedR.string.case_expected_body))
        }

        DemoCard(title = stringResource(SharedR.string.case_repeat_title)) {
            BodyText(stringResource(SharedR.string.case_repeat_body))
            PrimaryButton(
                title = stringResource(SharedR.string.case_relog),
                tag = SampleId.BTN_CASE_RELOG,
                onClick = { activity?.let { relogged = ScreenViewActions.relog(it, case) } },
            )
            relogged?.let { MonoText(it, tag = SampleId.TXT_CASE_RELOG_RESULT) }
            ScreenViewCases.next(case)?.let { next ->
                SecondaryButton(
                    title = stringResource(SharedR.string.case_push_next, next.label),
                    tag = SampleId.BTN_CASE_PUSH_NEXT,
                    onClick = { onPush(next) },
                )
            }
        }
    }
}
