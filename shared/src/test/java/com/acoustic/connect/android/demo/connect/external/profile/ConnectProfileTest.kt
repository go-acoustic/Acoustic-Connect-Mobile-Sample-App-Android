/*
 * Copyright (C) 2026 Acoustic, L.P. All rights reserved.
 *
 * Licensed under the Acoustic License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy
 * at https://www.acoustic.com/licenses/acoustic-license
 *
 * Sample app provided "as is", without warranty of any kind.
 */
package com.acoustic.connect.android.demo.connect.external.profile

import org.json.JSONException
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

/** What a profile file turns into, and what it writes to the SDK's configuration. */
class ConnectProfileTest {

    /** The SDK's configuration, as a map; [refused] keys are ones the SDK would not take. */
    private class FakeStore(
        initial: Map<String, String> = emptyMap(),
        private val refused: Set<String> = emptySet(),
    ) : ConnectConfigStore {

        val values = initial.toMutableMap()
        val writes = mutableListOf<Triple<String, String, String>>()

        override fun read(module: String, key: String): String? = values["$module.$key"]

        override fun write(module: String, key: String, value: String): Boolean {
            if ("$module.$key" in refused) return false
            writes += Triple(module, key, value)
            values["$module.$key"] = value
            return true
        }
    }

    @Test
    fun `a set replaces the key and keeps a string as it is`() {
        val profile = ConnectProfile.parse(
            "p",
            """{"set": {"Tealeaf": {"SessionTimeout": "1"}}}""",
        )
        val store = FakeStore()

        assertEquals(emptyList<String>(), profile.applyTo(store))

        assertEquals(listOf(Triple("Tealeaf", "SessionTimeout", "1")), store.writes)
    }

    @Test
    fun `a set writes a number, a boolean, an object and a list as JSON text`() {
        val profile = ConnectProfile.parse(
            "p",
            """{"set": {"Tealeaf": {"N": 5, "B": false, "O": {"a": [1, 2]}, "L": ["x", "y"]}}}""",
        )
        val store = FakeStore()

        profile.applyTo(store)

        assertEquals("5", store.values["Tealeaf.N"])
        assertEquals("false", store.values["Tealeaf.B"])
        assertTrue(JSONObject("""{"a": [1, 2]}""").similar(JSONObject(store.values.getValue("Tealeaf.O"))))
        assertEquals("""["x","y"]""", store.values["Tealeaf.L"])
    }

    @Test
    fun `a merge changes only the leaf it names`() {
        val layout = """{"GlobalScreenSettings": {"ScreenShot": true, "ScreenChange": true, "Masking": {"MaskIdList": ["a"]}}}"""
        val profile = ConnectProfile.parse(
            "p",
            """{"merge": {"Tealeaf": {"AutoLayout": {"GlobalScreenSettings": {"ScreenShot": false}}}}}""",
        )
        val store = FakeStore(mapOf("Tealeaf.AutoLayout" to layout))

        assertEquals(emptyList<String>(), profile.applyTo(store))

        val expected = """{"GlobalScreenSettings": {"ScreenShot": false, "ScreenChange": true, "Masking": {"MaskIdList": ["a"]}}}"""
        assertTrue(JSONObject(expected).similar(JSONObject(store.values.getValue("Tealeaf.AutoLayout"))))
    }

    @Test
    fun `a merge replaces a list instead of adding to it`() {
        val merged = mergeObjects(JSONObject("""{"l": ["a", "b"]}"""), JSONObject("""{"l": ["c"]}"""))

        assertTrue(JSONObject("""{"l": ["c"]}""").similar(merged))
    }

    @Test
    fun `a merge leaves the object it merges into unchanged`() {
        val base = JSONObject("""{"o": {"k": 1}}""")

        mergeObjects(base, JSONObject("""{"o": {"k": 2}, "n": 3}"""))

        assertTrue(JSONObject("""{"o": {"k": 1}}""").similar(base))
    }

    @Test
    fun `a merge into a key with no object is reported and writes nothing`() {
        val profile = ConnectProfile.parse(
            "p",
            """{"merge": {"Tealeaf": {"AutoLayout": {"A": 1}, "Other": {"B": 2}}}}""",
        )
        val store = FakeStore(mapOf("Tealeaf.Other" to "not json"))

        assertEquals(listOf("Tealeaf.AutoLayout", "Tealeaf.Other"), profile.applyTo(store))
        assertTrue(store.writes.isEmpty())
    }

    @Test
    fun `a write the SDK does not take is reported and the rest still go in`() {
        val profile = ConnectProfile.parse(
            "p",
            """{"set": {"Tealeaf": {"A": "1", "B": "2"}}}""",
        )
        val store = FakeStore(refused = setOf("Tealeaf.A"))

        assertEquals(listOf("Tealeaf.A"), profile.applyTo(store))
        assertEquals(listOf(Triple("Tealeaf", "B", "2")), store.writes)
    }

    @Test
    fun `sets go in before merges whatever the order of the file`() {
        val profile = ConnectProfile.parse(
            "p",
            """{"merge": {"Tealeaf": {"Layout": {"on": true}}}, "set": {"Tealeaf": {"Layout": "{\"on\": false, \"x\": 1}"}}}""",
        )
        val store = FakeStore()

        assertEquals(emptyList<String>(), profile.applyTo(store))

        assertTrue(JSONObject("""{"on": true, "x": 1}""").similar(JSONObject(store.values.getValue("Tealeaf.Layout"))))
    }

    @Test
    fun `the keys name each override by module and key`() {
        val profile = ConnectProfile.parse(
            "p",
            """{"set": {"Tealeaf": {"A": "1"}, "Other": {"B": "2"}}, "merge": {"Tealeaf": {"C": {}}}}""",
        )

        assertEquals(listOf("Tealeaf.A", "Other.B", "Tealeaf.C"), profile.keys)
        assertEquals("p", profile.name)
    }

    @Test
    fun `a description and a profile with no overrides are fine`() {
        val profile = ConnectProfile.parse("p", """{"description": "nothing"}""")

        assertEquals(emptyList<String>(), profile.keys)
        assertFalse(profile.applyTo(FakeStore()).isNotEmpty())
    }

    @Test
    fun `a file that is not a profile is refused`() {
        listOf(
            "not json",
            """{"sett": {"Tealeaf": {"A": "1"}}}""",
            """{"set": []}""",
            """{"set": {"Tealeaf": "1"}}""",
            """{"set": {"Tealeaf": {"A": null}}}""",
            """{"merge": {"Tealeaf": {"A": "1"}}}""",
        ).forEach { json ->
            assertThrows(json, JSONException::class.java) { ConnectProfile.parse("p", json) }
        }
    }
}
