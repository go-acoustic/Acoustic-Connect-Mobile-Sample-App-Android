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

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The cards print a payload as text and send it as a map; these hold the two together, so the
 * line a tester reads is always what went out.
 */
class ShowcasePayloadTest {

    @Test
    fun `the nested signal line shows the payload that is sent`() {
        val sent = JSONObject(ShowcaseActions.nestedSignalPayload() as Map<*, *>)
        val shown = JSONObject(ShowcaseActions.NESTED_PAYLOAD_TEXT)
        assertTrue("shown $shown, sent $sent", shown.similar(sent))
    }

    @Test
    fun `the flat signal line shows the payload that is sent`() {
        val sent = JSONObject(ShowcaseActions.flatSignalPayload() as Map<*, *>)
        val shown = JSONObject(ShowcaseActions.FLAT_PAYLOAD_TEXT)
        assertTrue("shown $shown, sent $sent", shown.similar(sent))
    }

    @Test
    fun `the nested payload keeps its number one level down`() {
        val payload = ShowcaseActions.nestedSignalPayload()
        assertTrue(payload.values.none { it is Number })
        assertEquals(3, (payload["cart"] as JSONObject).getInt("items"))
    }

    @Test
    fun `the custom event line shows the payload that is sent`() {
        val shown = JSONObject(ShowcaseActions.CUSTOM_EVENT_PAYLOAD_TEXT)
        val sent = ShowcaseActions.CUSTOM_EVENT_PAYLOAD
        assertEquals(shown.keySet(), sent.keys)
        sent.forEach { (key, value) -> assertEquals(value, shown.get(key).toString()) }
    }
}
