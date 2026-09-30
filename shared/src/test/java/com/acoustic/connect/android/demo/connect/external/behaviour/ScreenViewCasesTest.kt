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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The logged names must be byte-identical to the other platforms' samples, so these assertions
 * spell each one out rather than deriving it.
 */
class ScreenViewCasesTest {

    @Test
    fun `the matrix holds the eleven shared cases in order`() {
        assertEquals(
            listOf(
                "catalog", "product_details", "bid_confirmation", "checkout", "reserved_chars",
                "non_ascii", "long", "url_shaped", "whitespace", "empty", "null",
            ),
            ScreenViewCases.ALL.map { it.id },
        )
    }

    @Test
    fun `each case logs the exact shared name`() {
        val names = ScreenViewCases.ALL.associate { it.id to it.name }
        assertEquals("Catalog", names["catalog"])
        assertEquals("Product Details", names["product_details"])
        assertEquals("Bid Confirmation", names["bid_confirmation"])
        assertEquals("Checkout", names["checkout"])
        assertEquals("Order #4815 & Refund?ref=a/b", names["reserved_chars"])
        assertEquals("Płatności ✓ 🛒", names["non_ascii"])
        assertEquals("https://app.example.com/looks-like-a-url", names["url_shaped"])
        assertEquals("   ", names["whitespace"])
        assertEquals("", names["empty"])
        assertNull(names["null"])
    }

    @Test
    fun `the long name is exactly three hundred characters of the repeated lot`() {
        val long = ScreenViewCases.byId("long")?.name.orEmpty()
        assertEquals(300, long.length)
        assertTrue(long.startsWith("Lot 4815 Lot 4815 "))
        assertEquals(ScreenViewCases.LONG_NAME, long)
    }

    @Test
    fun `navigation cases never carry a blank or null name`() {
        assertEquals(9, ScreenViewCases.NAV.size)
        assertFalse(ScreenViewCases.NAV.any { it.name.isNullOrEmpty() })
    }

    @Test
    fun `only the direct path carries the empty and null names`() {
        assertEquals(7, ScreenViewCases.DIRECT.size)
        val directOnly = ScreenViewCases.ALL.filter { it.via == CaseVia.DIRECT }.map { it.id }
        assertEquals(listOf("empty", "null"), directOnly)
        assertFalse(ScreenViewCases.DIRECT.any { it.via == CaseVia.NAV })
    }

    @Test
    fun `describeName makes blank and null names visible`() {
        assertEquals("(null)", describeName(null))
        assertEquals("(empty string)", describeName(""))
        assertEquals("(whitespace ×3)", describeName("   "))
        assertEquals("Catalog", describeName("Catalog"))
    }

    @Test
    fun `describeName shortens a long name and says how long it was`() {
        val shown = describeName(ScreenViewCases.LONG_NAME)
        assertEquals(ScreenViewCases.LONG_NAME.take(45) + "… (300 chars)", shown)
    }
}
