/*
 * Copyright (C) 2026 Acoustic, L.P. All rights reserved.
 *
 * Licensed under the Acoustic License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy
 * at https://www.acoustic.com/licenses/acoustic-license
 *
 * Sample app provided "as is", without warranty of any kind.
 */
package com.acoustic.connect.android.demo.connect.external.analytics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ScreenViewTrailTest {

    @Test
    fun `the first screen has no referrer`() {
        assertNull(ScreenViewTrail().enter("Push"))
    }

    @Test
    fun `pushing a screen names the one below it`() {
        val trail = ScreenViewTrail()
        trail.enter("Showcase")
        assertEquals("Showcase", trail.enter("Showcase detail"))
        assertEquals("Showcase detail", trail.enter("Showcase detail 2"))
    }

    @Test
    fun `going back names the screen just left`() {
        val trail = ScreenViewTrail()
        trail.enter("Showcase")
        trail.enter("Showcase detail")
        assertEquals("Showcase detail", trail.enter("Showcase"))
    }
}
