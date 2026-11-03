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

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** Which profile a launch gets, and that the profiles the samples bundle do what they say. */
class ConnectProfilesTest {

    private class FakeStore(initial: Map<String, String> = emptyMap()) : ConnectConfigStore {

        val values = initial.toMutableMap()

        override fun read(module: String, key: String): String? = values["$module.$key"]

        override fun write(module: String, key: String, value: String): Boolean {
            values["$module.$key"] = value
            return true
        }
    }

    // Gradle runs a module's tests from the module's directory.
    private val assets = File("src/main/assets/connect-profiles")

    private fun bundled(path: String): String? =
        File("src/main/assets/$path").takeIf { it.isFile }?.readText()

    private fun bundledProfile(name: String): ConnectProfile {
        val resolution = ConnectProfiles.resolve(name, ::bundled)
        assertEquals(resolution.problem, "Profile: $name", resolution.status.label)
        return requireNotNull(resolution.profile)
    }

    @Test
    fun `no extra, or an empty one, is the default and reads no asset`() {
        listOf(null, "").forEach { requested ->
            val resolution = ConnectProfiles.resolve(requested) { error("read $it") }

            assertEquals("Profile: default", resolution.status.label)
            assertNull(resolution.profile)
            assertNull(resolution.problem)
        }
    }

    @Test
    fun `a bundled profile is named and parsed`() {
        val resolution = ConnectProfiles.resolve("one") { path ->
            """{"set": {"Tealeaf": {"A": "1"}}}""".takeIf { path == "connect-profiles/one.json" }
        }

        assertEquals("Profile: one", resolution.status.label)
        assertEquals(listOf("Tealeaf.A"), resolution.profile?.keys)
    }

    @Test
    fun `a name no profile has is unknown, applies nothing and does not throw`() {
        val resolution = ConnectProfiles.resolve("nope") { null }

        assertEquals("Profile: unknown nope", resolution.status.label)
        assertNull(resolution.profile)
        assertNotNull(resolution.problem)
    }

    @Test
    fun `a name that is no profile name is unknown without reading an asset`() {
        listOf("../ConnectBasicConfig", "a/b", "Upper", "with space", "-dash", "a.json").forEach { requested ->
            val resolution = ConnectProfiles.resolve(requested) { error("read $it") }

            assertEquals("Profile: unknown $requested", resolution.status.label)
            assertNull(resolution.profile)
        }
    }

    @Test
    fun `a profile file that is not valid is unknown`() {
        val resolution = ConnectProfiles.resolve("broken") { "{" }

        assertEquals("Profile: unknown broken", resolution.status.label)
        assertNull(resolution.profile)
        assertNotNull(resolution.problem)
    }

    @Test
    fun `the samples bundle exactly the three agreed profiles, each a valid one`() {
        val names = assets.listFiles().orEmpty().map { it.name.removeSuffix(".json") }.sorted()

        assertEquals(listOf("no-screenshots", "sdk-defaults", "session-timeout-1m"), names)
        names.forEach { bundledProfile(it) }
    }

    @Test
    fun `session-timeout-1m sets the SDK session timeout to one minute`() {
        val store = FakeStore()

        assertEquals(emptyList<String>(), bundledProfile("session-timeout-1m").applyTo(store))

        assertEquals(mapOf("Tealeaf.SessionTimeout" to "1"), store.values)
    }

    @Test
    fun `no-screenshots turns ScreenShot off in each sample's layout config and changes nothing else`() {
        listOf("sample-compose", "sample-xml").forEach { sample ->
            val layout = autoLayoutOf(File("../$sample/src/main/assets/ConnectLayoutConfig.json"))
            val store = FakeStore(mapOf("Tealeaf.AutoLayout" to layout.toString()))

            assertEquals(sample, emptyList<String>(), bundledProfile("no-screenshots").applyTo(store))

            val after = JSONObject(store.values.getValue("Tealeaf.AutoLayout"))
            assertEquals(sample, listOf(false), screenShotFlags(after))
            assertTrue(sample, layout.similar(withScreenShot(after, true)))
        }
    }

    @Test
    fun `sdk-defaults replaces the sample's masking with the library's own`() {
        val sample = autoLayoutOf(File("../sample-compose/src/main/assets/ConnectLayoutConfig.json"))
        val store = FakeStore(mapOf("Tealeaf.AutoLayout" to sample.toString()))

        assertEquals(emptyList<String>(), bundledProfile("sdk-defaults").applyTo(store))

        val applied = JSONObject(store.values.getValue("Tealeaf.AutoLayout"))
        assertEquals(listOf("GlobalScreenSettings", "MainActivity"), applied.keySet().sorted())
        assertFalse(applied.toString().contains("SECRET-"))
        assertTrue(store.values.getValue("Tealeaf.AppendMapIds").startsWith("{"))
    }

    private fun autoLayoutOf(file: File): JSONObject = JSONObject(file.readText()).getJSONObject("AutoLayout")

    /** Every `ScreenShot` value in [json], at any depth. */
    private fun screenShotFlags(json: Any): List<Boolean> = when (json) {
        is JSONObject -> json.keySet().flatMap { key ->
            if (key == "ScreenShot") listOf(json.getBoolean(key)) else screenShotFlags(json.get(key))
        }
        is JSONArray -> (0 until json.length()).flatMap { screenShotFlags(json.get(it)) }
        else -> emptyList()
    }

    /** A copy of [layout] with the global `ScreenShot` back at [value]. */
    private fun withScreenShot(layout: JSONObject, value: Boolean): JSONObject =
        JSONObject(layout.toString()).also {
            it.getJSONObject("GlobalScreenSettings").put("ScreenShot", value)
        }
}
