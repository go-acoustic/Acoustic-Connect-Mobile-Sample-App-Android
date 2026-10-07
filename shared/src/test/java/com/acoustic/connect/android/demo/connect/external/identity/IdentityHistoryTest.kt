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

import org.junit.Assert.assertEquals
import org.junit.Test

class IdentityHistoryTest {

    @Test
    fun `a new entry goes first`() {
        val history = listOf(IdentityHistoryEntry("Phone", "555"))
        assertEquals(
            listOf(IdentityHistoryEntry("Email", "a@example.com"), IdentityHistoryEntry("Phone", "555")),
            buildUpdatedHistory(history, "Email", "a@example.com"),
        )
    }

    @Test
    fun `logging a name again replaces its older value`() {
        val history = listOf(IdentityHistoryEntry("Email", "old@example.com"), IdentityHistoryEntry("Phone", "555"))
        assertEquals(
            listOf(IdentityHistoryEntry("Email", "new@example.com"), IdentityHistoryEntry("Phone", "555")),
            buildUpdatedHistory(history, "Email", "new@example.com"),
        )
    }

    @Test
    fun `history keeps the five newest`() {
        val history = (1..5).map { IdentityHistoryEntry("name$it", "v$it") }
        val updated = buildUpdatedHistory(history, "name0", "v0")
        assertEquals(5, updated.size)
        assertEquals(listOf("name0", "name1", "name2", "name3", "name4"), updated.map { it.name })
    }
}
