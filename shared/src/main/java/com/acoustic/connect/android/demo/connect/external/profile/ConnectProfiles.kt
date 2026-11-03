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

import android.content.Context
import android.util.Log
import com.acoustic.connect.android.connectmod.Connect
import org.json.JSONException
import java.io.IOException

/** Which profile the SDK was configured with, as the Behaviour hub shows it. */
sealed class ProfileStatus(val label: String) {

    /** No profile was asked for: the SDK runs on the bundled configuration. */
    object Default : ProfileStatus("Profile: default")

    /** [name] was found and its overrides were written to the SDK. */
    class Named(name: String) : ProfileStatus("Profile: $name")

    /** [name] is not a bundled profile: nothing was written. */
    class Unknown(name: String) : ProfileStatus("Profile: unknown $name")

    /** [name] was found but the SDK refused the writes of [keys]. */
    class NotApplied(name: String, keys: List<String>) : ProfileStatus("Profile: $name not applied: ${keys.joinToString()}")
}

/**
 * Lets a test launch the sample under a named SDK configuration, so one build can be run under
 * several. The profile is the string extra [EXTRA] of the launching intent; each profile is a file
 * in `assets/connect-profiles/` (see [ConnectProfile]), applied through the SDK's public
 * `Connect.updateConfig`.
 *
 * The configuration has to be in place before the SDK enables and before the first screen is laid
 * out, because the SDK reads its layout configuration once. A running process has passed both, so
 * only the first call per process counts; start the app from a stopped process to pick a profile.
 */
object ConnectProfiles {

    /** The intent extra that names the profile. Absent or empty: the bundled configuration. */
    const val EXTRA = "connectProfile"

    private const val ASSET_DIR = "connect-profiles"
    private const val TAG = "ConnectProfiles"
    private val VALID_NAME = Regex("[a-z0-9][a-z0-9-]*")

    private var resolved = false

    @Volatile
    private var status: ProfileStatus = ProfileStatus.Default

    /** The line the Behaviour hub shows: `Profile: <name>`, `Profile: default`, `Profile: unknown <name>` or `Profile: <name> not applied: <keys>`. */
    val label: String get() = status.label

    /**
     * Applies the profile [requested], once per process.
     *
     * The SDK's configuration only exists after `Connect.init`, so [initSdk] runs first when there
     * is a profile to write; a caller that has initialised the SDK already passes nothing. An
     * unknown or unreadable profile writes nothing, runs nothing and never throws.
     */
    @Synchronized
    fun applyOnce(context: Context, requested: String?, initSdk: () -> Unit = {}) {
        if (resolved) return
        resolved = true

        val resolution = resolve(requested) { path -> readAsset(context, path) }
        status = resolution.status
        resolution.problem?.let { Log.w(TAG, it) }

        val profile = resolution.profile ?: return
        initSdk()
        val failed = profile.applyTo(SdkConfigStore)
        if (failed.isEmpty()) {
            Log.i(TAG, "Profile ${profile.name} applied: ${profile.keys}")
        } else {
            status = ProfileStatus.NotApplied(profile.name, failed)
            Log.w(TAG, "Profile ${profile.name}: the SDK did not take $failed")
        }
    }

    internal class Resolution(
        val status: ProfileStatus,
        val profile: ConnectProfile?,
        val problem: String?,
    )

    /** The profile [requested] from [readAsset], which returns an asset's text or null when it has none. */
    internal fun resolve(requested: String?, readAsset: (path: String) -> String?): Resolution {
        if (requested.isNullOrEmpty()) return Resolution(ProfileStatus.Default, null, null)

        val unknown = ProfileStatus.Unknown(requested)
        if (!VALID_NAME.matches(requested)) {
            return Resolution(unknown, null, "Profile \"$requested\" is not a profile name")
        }
        val json = readAsset("$ASSET_DIR/$requested.json")
            ?: return Resolution(unknown, null, "Profile $requested is not bundled")

        return try {
            Resolution(ProfileStatus.Named(requested), ConnectProfile.parse(requested, json), null)
        } catch (e: JSONException) {
            Resolution(unknown, null, "Profile $requested is not a valid profile: ${e.message}")
        }
    }

    private fun readAsset(context: Context, path: String): String? =
        try {
            context.assets.open(path).bufferedReader().use { it.readText() }
        } catch (e: IOException) {
            null
        }

    /** The SDK's own configuration, through its public config API. */
    private object SdkConfigStore : ConnectConfigStore {

        override fun read(module: String, key: String): String? =
            Connect.getConfigItemString(key, module)

        override fun write(module: String, key: String, value: String): Boolean =
            Connect.updateConfig(key, value, module)
    }
}
