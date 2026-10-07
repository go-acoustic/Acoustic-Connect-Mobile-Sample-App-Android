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

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import com.acoustic.connect.android.connectmod.Connect
import com.acoustic.connect.android.demo.connect.external.AcousticCredentials
import org.json.JSONArray
import org.json.JSONObject

/**
 * The SDK calls behind the Showcase cards, and the result line each one prints.
 *
 * Both samples call these rather than Connect directly, so a card sends the same payload and shows
 * the same text in Compose and in XML — the result strings are what a UI test reads back. Every
 * call reports whether the SDK accepted the message for its queue, never whether the collector
 * received it; nothing on the device knows the latter.
 */
object ShowcaseActions {

    const val CUSTOM_EVENT_NAME = "demoCustomEvent"

    /**
     * Mixes all three value types React Native sends. `logCustomEvent` takes strings only on
     * Android, so the boolean and number travel as their text — the same "false" and "2" the other
     * samples' payloads arrive as.
     */
    val CUSTOM_EVENT_PAYLOAD: Map<String, String> = linkedMapOf(
        "tier" to "pro",
        "isTrial" to "false",
        "seats" to "2",
    )

    /** Shown on the card so a tester knows what to look for in the posted message. */
    const val CUSTOM_EVENT_PAYLOAD_TEXT = "{\n  \"tier\": \"pro\",\n  \"isTrial\": false,\n  \"seats\": 2\n}"

    const val EXCEPTION_MESSAGE = "Showcase: handled exception"
    private const val EXCEPTION_NAME = "ShowcaseException"

    const val DIALOG_TITLE = "Showcase dialog"
    const val DIALOG_MESSAGE = "Pick a button — each press is logged."

    fun sendCustomEvent(): String {
        val ok = Connect.logCustomEvent(CUSTOM_EVENT_NAME, HashMap<String?, String?>(CUSTOM_EVENT_PAYLOAD))
        return "${mark(ok)} queued $CUSTOM_EVENT_NAME — read customEvent in the posted message"
    }

    /**
     * The Connect-on-Connect shape: a `signalContent` object plus an `audience` array of
     * `{ name, value }` objects. Nesting has to be built from [JSONObject] and [JSONArray] — the
     * SDK's signal serialiser drops plain Kotlin maps and lists. `cart.items` sits one level down
     * because a top-level number needs Connect Android 11.0.24-beta or newer.
     */
    fun nestedSignalPayload(): HashMap<String?, Any?> = hashMapOf(
        "signalContent" to JSONObject()
            .put("signalType", "pageview")
            .put("url", "https://app.example.com/behaviour-demo")
            .put("pageCategory", "behaviour-demo"),
        "audience" to JSONArray()
            .put(JSONObject().put("name", "Account Name").put("value", "Acme Corp"))
            .put(JSONObject().put("name", "Account ID").put("value", "4815162342")),
        "cart" to JSONObject()
            .put("items", 3)
            .put("total", 24.99)
            .put("coupon", JSONObject.NULL),
    )

    /** Scalars only, so a tester can confirm the same call still works for existing callers. */
    fun flatSignalPayload(): HashMap<String?, Any?> = hashMapOf(
        "signalType" to "pageview",
        "pageCategory" to "behaviour-demo",
    )

    fun sendNestedSignal(): String = sendSignal("nested", NESTED_PAYLOAD_TEXT, nestedSignalPayload())

    fun sendFlatSignal(): String = sendSignal("flat", FLAT_PAYLOAD_TEXT, flatSignalPayload())

    // Written out rather than serialised from the payload: a HashMap has no stable key order, and
    // the line has to read the same in both samples and match the other platforms. A unit test
    // holds each one to its payload.
    internal const val NESTED_PAYLOAD_TEXT = "{\"signalContent\":{\"signalType\":\"pageview\"," +
        "\"url\":\"https://app.example.com/behaviour-demo\",\"pageCategory\":\"behaviour-demo\"}," +
        "\"audience\":[{\"name\":\"Account Name\",\"value\":\"Acme Corp\"}," +
        "{\"name\":\"Account ID\",\"value\":\"4815162342\"}]," +
        "\"cart\":{\"items\":3,\"total\":24.99,\"coupon\":null}}"
    internal const val FLAT_PAYLOAD_TEXT = "{\"signalType\":\"pageview\",\"pageCategory\":\"behaviour-demo\"}"

    /**
     * Uncaught exceptions are reported by the SDK on its own. This covers the other case: an
     * error the app caught and recovered from, which the SDK cannot see unless the app reports it.
     * `unhandled` is false because the app handled it.
     */
    fun logHandledException(): String = try {
        throw IllegalStateException(EXCEPTION_MESSAGE)
    } catch (error: IllegalStateException) {
        val ok = Connect.logExceptionEvent(
            EXCEPTION_NAME,
            error.message,
            error.stackTraceToString(),
            false,
        )
        "${mark(ok)} queued exception \"${error.message}\""
    }

    /**
     * Opens a platform [AlertDialog] and logs it the documented way: the layout when it shows,
     * and the button that closed it. The result arrives through [onResult] once a button is
     * pressed or the dialog is dismissed.
     */
    fun showDialog(activity: Activity, onResult: (String) -> Unit) {
        val listener = DialogInterface.OnClickListener { dialog, which ->
            Connect.logDialogEvent(dialog, which)
            onResult(if (which == DialogInterface.BUTTON_POSITIVE) "OK pressed" else "Cancel pressed")
        }
        val dialog = AlertDialog.Builder(activity)
            .setTitle(DIALOG_TITLE)
            .setMessage(DIALOG_MESSAGE)
            .setNegativeButton("Cancel", listener)
            .setPositiveButton("OK", listener)
            .setOnCancelListener { onResult("Dismissed") }
            .create()
        Connect.logScreenLayoutSetOnShowListener(activity, dialog)
        dialog.show()
    }

    fun disableSdk(): String {
        val ok = Connect.disable()
        return "disable() returned $ok — now navigate and check for further layout captures"
    }

    /**
     * `Connect.enable` returns nothing on Android, and the SDK finishes enabling a moment after
     * the call returns, so the line reports the call rather than a result it cannot know yet.
     */
    fun enableSdk(activity: Activity): String {
        val credentials = AcousticCredentials.load(activity)
        Connect.enable(credentials.appKey, credentials.collectorUrl)
        return "enable() called — capture resumes once the SDK finishes enabling"
    }

    private fun sendSignal(label: String, shown: String, payload: HashMap<String?, Any?>): String {
        val ok = Connect.logSignal(payload)
        return "${mark(ok)} $label — $shown"
    }

    private fun mark(ok: Boolean) = if (ok) "✓" else "✗"
}
