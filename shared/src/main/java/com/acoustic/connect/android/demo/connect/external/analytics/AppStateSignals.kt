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

import android.app.Activity
import android.app.Application
import android.content.ComponentCallbacks
import android.content.res.Configuration
import android.os.Bundle
import com.acoustic.connect.android.connectmod.Connect

/** Signal names — kept identical to the XML sample app so the two payloads line up. */
const val EVENT_APP_FOREGROUND = "appForeground"
const val EVENT_APP_BACKGROUND = "appBackground"
const val EVENT_ORIENTATION_CHANGE = "orientationChange"

/**
 * Process-level app-state instrumentation for the analytics signal audit, shared by both samples.
 *
 * <p>Activity lifecycle is deliberately left alone. Each sample already reaches
 * `Connect.onResume`/`Connect.onPause` by its own route — the Compose sample through
 * `ConnectComposeUI.ConnectWrapper`'s `ComposeUiLifecycle` observer, the Views sample through the
 * SDK's own `ActivityLifecycleCallbacks` (`EnableActivityLifeCycleListener: true` in
 * `ConnectAdvancedConfig.json`). Repeating it here is exactly the kind of double-count the
 * audit is looking for.
 *
 * <p>What neither route derives is *process* state. `onPause` fires whenever an activity pauses,
 * which is not the same as the app leaving the foreground, and nothing reports an orientation
 * change at all. Both are emitted here as custom events, so they are unambiguously attributable to
 * the app rather than to SDK auto-instrumentation when the two apps' payloads are compared.
 *
 * <p>Deliberately not called: `Connect.onPauseNoActivityInForeground()`. It is the SDK-native way to
 * say the process left the foreground, but an activity `onPause` has already been emitted by that
 * point, so whether the pair double-counts is a question for real payloads first.
 *
 * <p>This lives in `:shared` precisely because the point of two reference apps is that a
 * difference in their output means a difference in the SDK, not in the harness. Before the merge
 * there were two copies of this file and their comments had already drifted apart.
 */
object AppStateSignals {

    fun install(application: Application) {
        application.registerActivityLifecycleCallbacks(ForegroundTracker())
        application.registerComponentCallbacks(OrientationTracker(application.resources.configuration.orientation))
    }

    /**
     * Emits [name] as a custom event, tagged with the session it belongs to so the collector-side
     * payload can be matched back to a specific run.
     *
     * <p>Not gated on `Connect.isEnabled()`. In this integration that returns false for the whole
     * life of the process even while the SDK is initialised and posting — gating on it silently
     * dropped every app-state signal. The SDK's own return value is recorded instead, which is
     * also the more honest thing to show in the log.
     */
    internal fun emit(name: String, extras: Map<String, String> = emptyMap()) {
        val sessionId = currentSessionId()
        val payload = extras + ("sessionId" to sessionId)
        // The SDK declares the payload as HashMap<String?, String?>, so the nullable element types
        // have to be spelled out even though nothing here puts a null in it.
        val accepted = Connect.logCustomEvent(name, HashMap<String?, String?>(payload))
        SignalLog.record(name, describe(payload), accepted)
    }

    private fun describe(payload: Map<String, String>): String =
        payload.entries.joinToString(", ") { "${it.key}=${it.value}" }
}

/**
 * Reads a String getter that the SDK declares as non-null but does not always satisfy.
 *
 * <p>`Connect.getCurrentSessionId()` and `getCurrentLogicalPageName()` delegate to Tealeaf getters
 * that return null before a session or screen exists, and the SDK's own Kotlin null-check turns
 * that into a `NullPointerException` inside the getter. Reading the session id from
 * `Application.ActivityLifecycleCallbacks.onActivityStarted` therefore crashes the process on cold
 * launch, which is how this was found. Catching it here keeps the sample usable; the fix belongs in
 * the SDK.
 */
private inline fun readNullableSdkString(read: () -> String): String =
    try {
        read()
    } catch (npe: NullPointerException) {
        ""
    }

/*
 * Public, not internal: Kotlin `internal` is scoped to the compilation module, not to the Gradle
 * project tree, so `:sample-compose` and `:sample-xml` cannot see an internal member of `:shared`.
 * Both read these two from their App state screens (AppStateScreen.kt, AppStateFragment.kt), so
 * marking them internal fails the build with "Cannot access 'currentSessionId': it is internal".
 * `emit` above is a different case — nothing outside this module calls it, so it stays internal.
 */
fun currentSessionId(): String = readNullableSdkString { Connect.getCurrentSessionId() }

fun currentLogicalPageName(): String =
    readNullableSdkString { Connect.getCurrentLogicalPageName() }

/**
 * Foreground/background from the count of started activities.
 *
 * <p>A rotation stops and restarts the activity, which would otherwise read as a background trip
 * followed by a return. [Activity.isChangingConfigurations] identifies that stop, and the flag
 * carries the knowledge across to the matching start so neither half is reported.
 */
private class ForegroundTracker : Application.ActivityLifecycleCallbacks {

    private var startedActivities = 0
    private var restartingForConfigChange = false

    override fun onActivityStarted(activity: Activity) {
        val enteringForeground = startedActivities == 0 && !restartingForConfigChange
        startedActivities++
        restartingForConfigChange = false
        if (enteringForeground) {
            AppStateSignals.emit(EVENT_APP_FOREGROUND)
        }
    }

    override fun onActivityStopped(activity: Activity) {
        startedActivities--
        restartingForConfigChange = activity.isChangingConfigurations
        if (startedActivities == 0 && !restartingForConfigChange) {
            AppStateSignals.emit(EVENT_APP_BACKGROUND)
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
    override fun onActivityResumed(activity: Activity) = Unit
    override fun onActivityPaused(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
    override fun onActivityDestroyed(activity: Activity) = Unit
}

/**
 * Orientation changes, reported once per actual rotation.
 *
 * <p>`onConfigurationChanged` fires for any configuration delta — locale, font scale, dark mode —
 * so the previous orientation is held to filter out the changes that are not rotations.
 */
private class OrientationTracker(initialOrientation: Int) : ComponentCallbacks {

    private var lastOrientation = initialOrientation

    override fun onConfigurationChanged(newConfig: Configuration) {
        if (newConfig.orientation == lastOrientation) return
        val from = orientationName(lastOrientation)
        lastOrientation = newConfig.orientation
        AppStateSignals.emit(
            EVENT_ORIENTATION_CHANGE,
            mapOf("from" to from, "to" to orientationName(newConfig.orientation)),
        )
    }

    override fun onLowMemory() = Unit

    private fun orientationName(orientation: Int): String = when (orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> "landscape"
        Configuration.ORIENTATION_PORTRAIT -> "portrait"
        else -> "undefined"
    }
}
