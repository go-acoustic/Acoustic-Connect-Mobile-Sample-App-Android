/*
 * Copyright (C) 2026 Acoustic, L.P. All rights reserved.
 *
 * Licensed under the Acoustic License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy
 * at https://www.acoustic.com/licenses/acoustic-license
 *
 * Sample app provided "as is", without warranty of any kind.
 */
package com.acoustic.connect.android.demo.connect.external.contract

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ScreenNameTest {

    @Test
    fun `the first detail screen carries no number`() {
        assertEquals("Showcase detail", ScreenName.showcaseDetail(1))
    }

    @Test
    fun `deeper detail screens are numbered by depth`() {
        assertEquals("Showcase detail 2", ScreenName.showcaseDetail(2))
        assertEquals("Showcase detail 5", ScreenName.showcaseDetail(ScreenName.MAX_SHOWCASE_DEPTH))
    }

    @Test
    fun `the chain is capped at five`() {
        assertEquals(5, ScreenName.MAX_SHOWCASE_DEPTH)
        assertThrows(IllegalArgumentException::class.java) { ScreenName.showcaseDetail(6) }
        assertThrows(IllegalArgumentException::class.java) { ScreenName.showcaseDetail(0) }
    }
}
