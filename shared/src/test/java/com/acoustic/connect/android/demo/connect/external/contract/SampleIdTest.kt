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
import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.reflect.Modifier

/** Guards the automation contract against two constants silently sharing one locator. */
class SampleIdTest {

    private val ids: Map<String, String> = SampleId::class.java.declaredFields
        .filter { Modifier.isStatic(it.modifiers) && it.type == String::class.java }
        .associate { it.name to it.get(null) as String }

    @Test
    fun `every identifier is distinct`() {
        val duplicates = ids.values.groupingBy { it }.eachCount().filterValues { it > 1 }
        assertTrue("duplicated ids: $duplicates", duplicates.isEmpty())
    }

    @Test
    fun `every identifier is a valid Android resource name`() {
        val invalid = ids.filterValues { !it.matches(Regex("[a-z][a-z0-9_]*")) }
        assertTrue("not usable as android:id: $invalid", invalid.isEmpty())
    }

    @Test
    fun `case buttons use the React Native id shape`() {
        assertEquals("btn_screenview_catalog", SampleId.screenViewButton("catalog"))
        assertEquals("btn_navigate_non_ascii", SampleId.navigateButton("non_ascii"))
    }
}
