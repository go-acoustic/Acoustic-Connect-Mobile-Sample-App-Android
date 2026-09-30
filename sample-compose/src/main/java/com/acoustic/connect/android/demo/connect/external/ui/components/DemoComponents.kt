/*
 * Copyright (C) 2026 Acoustic, L.P. All rights reserved.
 *
 * Licensed under the Acoustic License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy
 * at https://www.acoustic.com/licenses/acoustic-license
 *
 * Sample app provided "as is", without warranty of any kind.
 */
package com.acoustic.connect.android.demo.connect.external.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.acoustic.connect.android.connectmod.composeui.customcomposable.LoggedButton
import com.acoustic.connect.android.connectmod.composeui.customcomposable.LoggedOutlinedTextField
import com.acoustic.connect.android.connectmod.composeui.customcomposable.LoggedText
import com.acoustic.connect.android.demo.connect.external.R
import com.acoustic.connect.android.demo.connect.external.ui.theme.BrandBackground
import com.acoustic.connect.android.demo.connect.external.ui.theme.DarkGrey
import com.acoustic.connect.android.demo.connect.external.ui.theme.LightGrey
import com.acoustic.connect.android.demo.connect.external.ui.theme.MiddleGrey
import com.acoustic.connect.android.demo.connect.external.ui.theme.Periwinkle
import com.acoustic.connect.android.demo.connect.external.ui.theme.Violet

/*
 * The building blocks every screen is made of, mirroring the React Native sample's components of
 * the same names so the two lay out alike. Each takes the contract id it publishes as `tag`; the
 * root of the app switches `testTagsAsResourceId` on, which is what lets Appium match them.
 */

/** Adds the contract id, when there is one, as the element's test tag. */
fun Modifier.contractTag(tag: String?): Modifier =
    if (tag == null) this else semantics { testTag = tag }

/** A scrolling screen body on the brand background, with its cards spaced as React Native's. */
@Composable
fun DemoScreen(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BrandBackground)
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        content = content,
    )
}

@Composable
fun LogoHeader(title: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(id = R.drawable.acoustic_logo_light),
            contentDescription = "Acoustic Connect Logo",
            modifier = Modifier.width(220.dp),
        )
        LoggedText(text = title, color = Violet, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun DemoCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            LoggedText(text = title, color = Violet, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            content()
        }
    }
}

@Composable
fun BodyText(text: String) {
    LoggedText(text = text, color = DarkGrey, fontSize = 13.sp, lineHeight = 19.sp)
}

/** Monospaced output — a result line, a payload, a logged name. */
@Composable
fun MonoText(text: String, tag: String? = null) {
    LoggedText(
        text = text,
        color = Violet,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        fontFamily = FontFamily.Monospace,
        modifier = Modifier.contractTag(tag),
    )
}

/** Grey panel for a hint or a payload; [accent] adds React Native's coloured left bar. */
@Composable
fun NoteBox(
    accent: Color? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(8.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(LightGrey)
            .then(if (accent != null) Modifier.leftBar(accent) else Modifier)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        content = content,
    )
}

@Composable
fun HintText(text: String) {
    LoggedText(text = text, color = Violet, fontSize = 12.sp, lineHeight = 18.sp)
}

@Composable
fun PrimaryButton(
    title: String,
    tag: String?,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    LoggedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().contractTag(tag),
        enabled = enabled,
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Periwinkle,
            contentColor = Color.White,
            disabledContainerColor = MiddleGrey,
            disabledContentColor = Color.White,
        ),
        // A stable identifier, so the control id the SDK derives from it reads the same on every
        // run instead of embedding a random one.
        logIdentifier = tag ?: title,
        buttonText = title,
    ) {
        Text(title, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SecondaryButton(
    title: String,
    tag: String?,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    LoggedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().contractTag(tag),
        enabled = enabled,
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = Periwinkle,
            disabledContainerColor = Color.White,
            disabledContentColor = MiddleGrey,
        ),
        border = BorderStroke(1.dp, if (enabled) Periwinkle else MiddleGrey),
        logIdentifier = tag ?: title,
        buttonText = title,
    ) {
        Text(title, fontWeight = FontWeight.SemiBold)
    }
}

/**
 * An outlined text field. [maskLabel] is what the SDK's Compose capture matches against
 * `MaskAccessibilityLabelList` in ConnectLayoutConfig.json to decide whether to mask the value.
 */
@Composable
fun DemoTextField(
    label: String,
    placeholder: String,
    value: String,
    tag: String?,
    maskLabel: String? = null,
    onValueChange: (String) -> Unit,
) {
    LoggedOutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth().contractTag(tag),
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        singleLine = true,
        shape = RoundedCornerShape(10.dp),
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.None, autoCorrect = false),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = Color.White,
            focusedContainerColor = Color.White,
            focusedBorderColor = Periwinkle,
            unfocusedBorderColor = MiddleGrey,
        ),
        maskLabel = maskLabel,
    )
}

private val ACCENT_BAR_WIDTH = 3.dp

private fun Modifier.leftBar(color: Color): Modifier = drawBehind {
    drawRect(color = color, size = Size(ACCENT_BAR_WIDTH.toPx(), size.height))
}
