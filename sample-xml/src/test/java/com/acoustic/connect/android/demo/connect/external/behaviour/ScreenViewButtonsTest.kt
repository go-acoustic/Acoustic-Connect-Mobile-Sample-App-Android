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

import org.junit.Assert.assertEquals
import org.junit.Test

/** Holds the code-built Screen Views buttons to the shared matrix, case for case. */
class ScreenViewButtonsTest {

    @Test
    fun `every navigation case has a navigate button id`() {
        assertEquals(ScreenViewCases.NAV.map { it.id }, ScreenViewButtons.navigate.keys.toList())
    }

    @Test
    fun `every direct case has a screen-view button id`() {
        assertEquals(ScreenViewCases.DIRECT.map { it.id }, ScreenViewButtons.direct.keys.toList())
    }
}
