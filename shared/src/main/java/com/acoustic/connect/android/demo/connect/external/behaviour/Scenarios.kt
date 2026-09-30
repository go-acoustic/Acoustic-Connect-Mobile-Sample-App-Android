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

/**
 * Registry of the fixes the Verification screen checks, and — just as important — whether each one
 * is actually verifiable against the SDK build the app is running. Reserved: the Verification
 * screen that renders it comes later.
 *
 * The registry is shared with the React Native and iOS samples, text included, so a scenario key
 * quoted in a support thread means the same card everywhere. That is why React Native bridge fixes
 * appear here too, and why no card is filtered by platform: an Android-only card still renders on
 * iOS and the other way round.
 *
 * [Scenario.blockedBy] is set when the fix exists in source but no published native artifact
 * carries it yet. Those cards still render, deliberately: running them captures the failing
 * baseline, which is what makes the later re-run meaningful. Reading a blocked card as a pass is
 * the trap this field exists to prevent.
 */
enum class ScenarioChannel(val label: String) {
    /** A React Native bridge fix; ships with the npm release. */
    REACT_NATIVE("React Native SDK"),

    /** The Connect iOS pod; present only once a pod containing the commit is published. */
    IOS_NATIVE("iOS native SDK"),

    /** The Connect Android artifact; same lag as the iOS pod. */
    ANDROID_NATIVE("Android native SDK"),

    /** Both native SDKs at once, each with its own lag. */
    NATIVE("iOS + Android native SDK"),

    /** Proven by the app building and running at all, with nothing to tap. */
    BUILD("Build-time"),
}

enum class ScenarioPlatform(val label: String) {
    IOS("ios"),
    ANDROID("android"),
    BOTH("both"),
}

data class Scenario(
    /** Stable, descriptive id — safe to quote in a support thread. */
    val key: String,
    val title: String,
    /** What the tester does. */
    val action: String,
    /** What a fixed build produces. */
    val expected: String,
    val channel: ScenarioChannel,
    val platform: ScenarioPlatform,
    /** Set when no published artifact carries the fix — the card is a baseline, not a test. */
    val blockedBy: String? = null,
)

object Scenarios {

    const val BLOCKED_BANNER_TITLE = "Baseline only — fix not published"

    val CUSTOM_EVENT_VALUE_TYPES = Scenario(
        key = "custom-event-value-types",
        title = "Custom-event values keep their type",
        action = "Send a custom event carrying a string, a boolean and a number.",
        expected = "Values arrive unwrapped — \"pro\", \"true\", \"2.0\" — not the Kotlin " +
            "data-class form \"Second(value=pro)\". Android was the broken platform; iOS already " +
            "unwrapped correctly, so the two should now agree.",
        channel = ScenarioChannel.REACT_NATIVE,
        platform = ScenarioPlatform.BOTH,
    )

    val SIGNAL_NESTED_JSON = Scenario(
        key = "signal-nested-json",
        title = "logSignal accepts nested JSON",
        action = "Send the nested signal payload (object + array of objects).",
        expected = "Nesting survives to the collector on both platforms. The payload keeps its " +
            "numbers one level down, which is portable across every supported Connect Android " +
            "version; a top-level number needs Connect Android 11.0.24-beta or newer, having " +
            "been dropped by the SDK serializer before that.",
        channel = ScenarioChannel.REACT_NATIVE,
        platform = ScenarioPlatform.BOTH,
    )

    val IDENTITY_LOGIN_METHOD_DEFAULT = Scenario(
        key = "identity-login-method-default",
        title = "loggedIn defaults to loginMethod",
        action = "Log an identity with both the signal type and the parameters omitted, so the " +
            "bridge has to supply its own defaults.",
        expected = "The signal carries loginMethod: email. Before the fix the loggedIn default " +
            "was paired with registrationMethod, so every defaulted identity call emitted the " +
            "wrong attribute.",
        channel = ScenarioChannel.REACT_NATIVE,
        platform = ScenarioPlatform.BOTH,
    )

    val LAYOUT_CONFIG_APPLIED = Scenario(
        key = "layout-config-applied",
        title = "Layout config from ConnectConfig.json is applied",
        action = "Type into the masked field below, then read the value in the posted layout " +
            "message.",
        expected = "The value arrives masked. The config block is named layoutConfigIos / " +
            "layoutConfigAndroid; the bridge used to look for a plain \"layoutConfig\" key and so " +
            "applied nothing at all.",
        channel = ScenarioChannel.REACT_NATIVE,
        platform = ScenarioPlatform.BOTH,
    )

    val ANDROID_COMPILE_CLASSPATH = Scenario(
        key = "android-compile-classpath",
        title = "eocore/tealeaf on the Android compile classpath",
        action = "Nothing to tap — this one is proven by the app building and running at all.",
        expected = "The Android module compiles against com.ibm.eo / com.tl types. Connect marks " +
            "them runtime-scope in its POM, so they need compileOnly + testCompileOnly entries to " +
            "be visible at compile time.",
        channel = ScenarioChannel.BUILD,
        platform = ScenarioPlatform.ANDROID,
    )

    val REPLAY_CAPTURES_MODAL = Scenario(
        key = "replay-captures-modal",
        title = "Session replay captures React Native <Modal>",
        action = "Open each modal, interact, and close it.",
        expected = "The replay carries a populated control tree for the modal, not an empty one. " +
            "A React Native <Modal> presents outside the navigator hierarchy, which is why it " +
            "took a separate capture path.",
        channel = ScenarioChannel.IOS_NATIVE,
        platform = ScenarioPlatform.IOS,
    )

    val SCREENVIEW_REFERRER = Scenario(
        key = "screenview-referrer",
        title = "Screenview referrer points at the previous screen",
        action = "Move between screens in Screen Views and read the referrer on each screenview.",
        expected = "referrer is the screen you came from. The iOS bug set it to the screen's own " +
            "name on every event, which collapses a whole session into one replay step. Android " +
            "already chained it correctly.",
        channel = ScenarioChannel.IOS_NATIVE,
        platform = ScenarioPlatform.IOS,
        blockedBy = "Fixed in iOS source on 2026-08-20, but the newest published pod " +
            "(AcousticConnectDebug 2.1.18) was tagged 2026-07-29. Running this today records the " +
            "failing baseline.",
    )

    val WEBVIEW_POST_NOT_REPLAYED_AS_GET = Scenario(
        key = "webview-post-not-replayed-as-get",
        title = "WebView form POST is not replayed as GET",
        action = "Submit the form in the WebView screen.",
        expected = "The echo shows method POST. The capture reload used to re-issue the current " +
            "URL as a GET, so a payment submission came back 405 Method Not Allowed.",
        channel = ScenarioChannel.ANDROID_NATIVE,
        platform = ScenarioPlatform.ANDROID,
        blockedBy = "This harness does not reproduce the 405, and the shipped SDK test explains " +
            "why: WebView never calls shouldOverrideUrlLoading for a main-frame form POST, so a " +
            "normal submission cannot trigger the conversion — the fix's own test drives the " +
            "hazard directly instead. What this harness DID establish: setting " +
            "GoogleWebViewEnabled false suppresses WebView instrumentation completely (Found " +
            "Webview 11 -> 0, RNCWebView nodes 3 -> 0, with the screen demonstrably visited), " +
            "which is the customer's missing workaround. On connect 11.0.18-beta — which does NOT " +
            "carry the fix — the POST survives identically, with WebView capture demonstrably " +
            "engaged (capture JS injected, RNCWebView nodes in the layout) and after an explicit " +
            "logScreenLayout on the POST result. So a pass here says nothing about the fix. Two " +
            "earlier leads here have since been measured and can be dropped: WebView discovery " +
            "does work on this screen (Found Webview fires and RNCWebView nodes appear in the " +
            "layout) once the WebView is scrolled into the viewport — the zero-hit readings came " +
            "from capturing while it sat below the fold, which the tree-walk skips by design. " +
            "What remains real is that the SDK logs \"WebView Id is: null\" for an RN-hosted " +
            "WebView, so the DOM-capture DCID it waits for can never be matched, and a capture " +
            "that does find the WebView currently drops the screen's whole layout message. That " +
            "is a native-SDK defect, tracked separately.",
    )

    val ACCESSIBILITY_LABEL_MASKING = Scenario(
        key = "accessibility-label-masking",
        title = "Masking covers the accessibility label and hint",
        action = "Drive a capture on the card below, then read the `accessibility` object of each " +
            "row in the posted layout message.",
        expected = "No row carries the address in `accessibility.label`. Masking used to redact " +
            "an element's value only and serialise the accessibility object verbatim, so on React " +
            "Native — where a <Text> node's label defaults to its own content — a masked address " +
            "still travelled in the label. `accessibility.id` is still present and unredacted, " +
            "deliberately: it identifies the element rather than describing it. Needs Connect iOS " +
            "2.1.22+ or Android 11.0.23-beta+ — both published, and AndroidVersion / iOSVersion " +
            "are empty in ConnectConfig.example.json, so an unpinned sample resolves them. " +
            "Against a pinned older SDK this records the failing baseline instead.",
        channel = ScenarioChannel.NATIVE,
        platform = ScenarioPlatform.BOTH,
    )

    /** Every scenario, in the React Native registry's order. */
    val ALL: List<Scenario> = listOf(
        CUSTOM_EVENT_VALUE_TYPES,
        SIGNAL_NESTED_JSON,
        IDENTITY_LOGIN_METHOD_DEFAULT,
        LAYOUT_CONFIG_APPLIED,
        ANDROID_COMPILE_CLASSPATH,
        REPLAY_CAPTURES_MODAL,
        SCREENVIEW_REFERRER,
        WEBVIEW_POST_NOT_REPLAYED_AS_GET,
        ACCESSIBILITY_LABEL_MASKING,
    )
}
