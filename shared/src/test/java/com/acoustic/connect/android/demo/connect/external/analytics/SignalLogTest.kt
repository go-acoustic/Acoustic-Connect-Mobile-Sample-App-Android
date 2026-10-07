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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SignalLogTest {

    /** [SignalLog] is a singleton, so each test starts from a known state. */
    @Before
    fun resetLog() {
        SignalLog.clear()
    }

    @Test
    fun `starts empty`() {
        assertTrue(SignalLog.entries.value.isEmpty())
    }

    @Test
    fun `orders entries newest first`() {
        SignalLog.record("first")
        SignalLog.record("second")
        SignalLog.record("third")

        assertEquals(
            listOf("third", "second", "first"),
            SignalLog.entries.value.map { it.name },
        )
    }

    @Test
    fun `keeps detail and outcome alongside the name`() {
        SignalLog.record("appBackground", "sessionId=abc", accepted = true)

        val entry = SignalLog.entries.value.single()
        assertEquals("appBackground", entry.name)
        assertEquals("sessionId=abc", entry.detail)
        assertEquals(true, entry.accepted)
    }

    @Test
    fun `records a not-sent call as null rather than as a failure`() {
        SignalLog.record("appForeground", "not sent — SDK not enabled yet", accepted = null)

        assertNull(SignalLog.entries.value.single().accepted)
    }

    @Test
    fun `caps the log and drops the oldest entries`() {
        repeat(60) { index -> SignalLog.record("event$index") }

        val entries = SignalLog.entries.value
        assertEquals(50, entries.size)
        assertEquals("event59", entries.first().name)
        assertEquals("event10", entries.last().name)
    }

    @Test
    fun `clear empties the log`() {
        SignalLog.record("appForeground")
        SignalLog.clear()

        assertTrue(SignalLog.entries.value.isEmpty())
    }
}
