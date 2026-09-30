/*
 * Copyright (C) 2026 Acoustic, L.P. All rights reserved.
 *
 * Licensed under the Acoustic License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy
 * at https://www.acoustic.com/licenses/acoustic-license
 *
 * Sample app provided "as is", without warranty of any kind.
 */
package com.acoustic.connect.android.demo.connect.external.identity

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.acoustic.connect.android.connectmod.composeui.customcomposable.LoggedText
import com.acoustic.connect.android.demo.connect.external.analytics.ScreenviewUnloadEffect
import com.acoustic.connect.android.demo.connect.external.contract.SampleId
import com.acoustic.connect.android.demo.connect.external.contract.ScreenName
import com.acoustic.connect.android.demo.connect.external.ui.components.DemoCard
import com.acoustic.connect.android.demo.connect.external.ui.components.DemoScreen
import com.acoustic.connect.android.demo.connect.external.ui.components.DemoTextField
import com.acoustic.connect.android.demo.connect.external.ui.components.LogoHeader
import com.acoustic.connect.android.demo.connect.external.ui.components.PrimaryButton
import com.acoustic.connect.android.demo.connect.external.ui.components.contractTag
import com.acoustic.connect.android.demo.connect.external.ui.theme.DarkGrey
import com.acoustic.connect.android.demo.connect.external.ui.theme.LightGrey
import com.acoustic.connect.android.demo.connect.external.ui.theme.Periwinkle
import com.acoustic.connect.android.demo.connect.external.ui.theme.Violet
import com.acoustic.connect.android.demo.connect.external.shared.R as SharedR

/**
 * Identity tab. Logs a `loggedIn` or `accountRegistered` signal, shows the last result, and lists
 * the five most recent identifier pairs so a known-good combination can be re-sent quickly.
 */
@Composable
fun IdentityScreen(viewModel: IdentityViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    ScreenviewUnloadEffect(ScreenName.IDENTITY)

    DemoScreen {
        LogoHeader(title = stringResource(SharedR.string.identity_header))

        state.lastResult?.let { result ->
            DemoCard(title = stringResource(SharedR.string.identity_last_result)) {
                LoggedText(
                    text = result,
                    color = DarkGrey,
                    fontSize = 14.sp,
                    modifier = Modifier.contractTag(SampleId.TXT_IDENTITY_RESULT),
                )
            }
        }

        DemoCard(title = stringResource(SharedR.string.identity_log_title)) {
            DemoTextField(
                label = stringResource(SharedR.string.identity_name_label),
                placeholder = stringResource(SharedR.string.identity_name_placeholder),
                value = state.identifierName,
                tag = SampleId.FIELD_IDENTIFIER_NAME,
                onValueChange = viewModel::onIdentifierNameChanged,
            )
            DemoTextField(
                label = stringResource(SharedR.string.identity_value_label),
                placeholder = stringResource(SharedR.string.identity_value_placeholder),
                value = state.identifierValue,
                tag = SampleId.FIELD_IDENTIFIER_VALUE,
                // Matched against MaskAccessibilityLabelList, so the captured layout carries the
                // value masked — the XML sample masks the same field by id.
                maskLabel = IDENTIFIER_VALUE_MASK_LABEL,
                onValueChange = viewModel::onIdentifierValueChanged,
            )
            PrimaryButton(
                title = stringResource(SharedR.string.identity_log_logged_in),
                tag = SampleId.BTN_SEND_IDENTITY_SIGNAL,
                enabled = state.canLog,
                onClick = viewModel::onLogLoggedIn,
            )
            PrimaryButton(
                title = stringResource(SharedR.string.identity_log_registered),
                tag = SampleId.BTN_SEND_ACCOUNT_REGISTERED_SIGNAL,
                enabled = state.canLog,
                onClick = viewModel::onLogAccountRegistered,
            )
        }

        if (state.history.isNotEmpty()) {
            DemoCard(title = stringResource(SharedR.string.identity_recent)) {
                Column {
                    state.history.forEachIndexed { index, entry ->
                        if (index > 0) HorizontalDivider(color = LightGrey)
                        HistoryRow(entry = entry, onClick = { viewModel.onHistoryEntrySelected(entry) })
                    }
                }
            }
        }
    }
}

/** The label the layout config's MaskAccessibilityLabelList names for the identifier value. */
private const val IDENTIFIER_VALUE_MASK_LABEL = "Identifier Value"

@Composable
private fun HistoryRow(entry: IdentityHistoryEntry, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            LoggedText(text = entry.name, color = Violet, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            LoggedText(text = entry.value, color = DarkGrey, fontSize = 12.sp)
        }
        LoggedText(text = "↖", color = Periwinkle, fontSize = 16.sp)
    }
}
