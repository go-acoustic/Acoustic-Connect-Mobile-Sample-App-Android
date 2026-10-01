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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.acoustic.connect.android.connectmod.composeui.customcomposable.LoggedText
import com.acoustic.connect.android.demo.connect.external.contract.SampleId
import com.acoustic.connect.android.demo.connect.external.ui.components.BodyText
import com.acoustic.connect.android.demo.connect.external.ui.components.DemoCard
import com.acoustic.connect.android.demo.connect.external.ui.components.DemoTextField
import com.acoustic.connect.android.demo.connect.external.ui.components.PrimaryButton
import com.acoustic.connect.android.demo.connect.external.ui.components.SecondaryButton
import com.acoustic.connect.android.demo.connect.external.ui.components.contractTag
import com.acoustic.connect.android.demo.connect.external.ui.theme.BrandBackground
import com.acoustic.connect.android.demo.connect.external.ui.theme.Violet
import com.acoustic.connect.android.demo.connect.external.shared.R as SharedR

/** React Native's dimmed backdrop: violet at 45% opacity. */
private val DimmedBackdrop = Color(0x731F1E5D)

/**
 * Session-replay reference for a modal.
 *
 * A Compose `Dialog` does not render inline: it opens a window of its own with its own
 * composition, outside the activity's view tree and outside ConnectWrapper. That makes it the
 * Android counterpart of React Native's `<Modal>` — the case where layout capture has to find
 * content that is not under the screen it is capturing. The opaque variant covers the screen; the
 * transparent one dims it and leaves it visible behind the sheet.
 *
 * The controls are deliberately varied — text, an input, two buttons and a status line — so a
 * replay reviewer can confirm each is individually inspectable, not just visible in a screenshot.
 * The status line echoes the input and the tap count, so an interaction that never registered can
 * be told apart from one the SDK failed to capture.
 */
@Composable
fun ReplayModalCard(transparent: Boolean) {
    var visible by rememberSaveable { mutableStateOf(false) }
    var note by rememberSaveable { mutableStateOf("") }
    var actionCount by rememberSaveable { mutableIntStateOf(0) }

    val title = if (transparent) SharedR.string.showcase_modal_transparent_title else SharedR.string.showcase_modal_opaque_title
    DemoCard(title = stringResource(title)) {
        BodyText(stringResource(SharedR.string.showcase_modal_body))
        PrimaryButton(
            title = stringResource(SharedR.string.showcase_open_modal),
            tag = if (transparent) SampleId.BTN_OPEN_REPLAY_MODAL_TRANSPARENT else SampleId.BTN_OPEN_REPLAY_MODAL_OPAQUE,
            onClick = { visible = true },
        )
    }

    if (visible) {
        Dialog(
            onDismissRequest = { visible = false },
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    // A dialog is a separate composition, so the root's switch does not reach it.
                    .semantics { testTagsAsResourceId = true }
                    .background(if (transparent) DimmedBackdrop else BrandBackground)
                    .padding(20.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(14.dp))
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    LoggedText(text = stringResource(SharedR.string.showcase_modal_content_title), color = Violet, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                    BodyText(stringResource(SharedR.string.showcase_modal_content_body))
                    DemoTextField(
                        label = stringResource(SharedR.string.showcase_modal_note_label),
                        placeholder = stringResource(SharedR.string.showcase_modal_note_placeholder),
                        value = note,
                        tag = SampleId.FIELD_REPLAY_MODAL_NOTE,
                        onValueChange = { note = it },
                    )
                    PrimaryButton(
                        title = stringResource(SharedR.string.showcase_modal_primary_action),
                        tag = SampleId.BTN_REPLAY_MODAL_ACTION,
                        onClick = { actionCount += 1 },
                    )
                    SecondaryButton(
                        title = stringResource(SharedR.string.showcase_modal_close),
                        tag = SampleId.BTN_CLOSE_REPLAY_MODAL,
                        onClick = { visible = false },
                    )
                    LoggedText(
                        text = stringResource(SharedR.string.showcase_modal_status, note.ifEmpty { "—" }, actionCount),
                        color = Violet,
                        fontSize = 12.sp,
                        modifier = Modifier.contractTag(SampleId.TXT_REPLAY_MODAL_RESULT),
                    )
                }
            }
        }
    }
}
