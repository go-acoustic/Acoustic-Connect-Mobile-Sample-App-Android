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

/** The exact-name card's lines, which a UI test reads back in both samples. */
class ScreenViewActionsTest {

    private val empty = requireNotNull(ScreenViewCases.byId("empty"))

    @Test
    fun `a line shows the mark, the case id and the name as described`() {
        assertEquals("✓ empty → (empty string)", ScreenViewActions.logLine(empty, queued = true))
        assertEquals("✗ empty → (empty string)", ScreenViewActions.logLine(empty, queued = false))
    }

    @Test
    fun `the log keeps the newest twelve lines, newest first`() {
        val log = (1..15).fold(emptyList<String>()) { acc, n -> ScreenViewActions.prepend(acc, "line $n") }
        assertEquals(ScreenViewActions.LOG_LIMIT, log.size)
        assertEquals("line 15", log.first())
        assertEquals("line 4", log.last())
    }
}
