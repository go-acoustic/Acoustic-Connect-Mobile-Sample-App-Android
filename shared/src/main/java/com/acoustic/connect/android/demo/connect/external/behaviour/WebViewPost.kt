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
import com.acoustic.connect.android.connectmod.Connect
import com.acoustic.connect.android.demo.connect.external.contract.ScreenName

/**
 * The WebView POST check, shared by both samples: the form, the endpoint and the status lines.
 *
 * It probes whether the SDK's WebView capture turns a form POST into a GET. The capture path
 * reloaded the WebView's current URL to grab the layout, and a reload of a POST result re-issues
 * it as a GET; the customer saw that as HTTP 405 on payment submission. The endpoint here only
 * answers POST, so the failure is unambiguous — a GET comes back 405, the customer's exact symptom.
 *
 * The form is inline HTML, so the only network dependency is the echo endpoint itself. Point
 * [ECHO_URL] somewhere else if this environment has no route to the public internet.
 */
object WebViewPost {

    const val ECHO_URL = "https://httpbin.org/post"
    const val BASE_URL = "https://httpbin.org"
    private const val METHOD_NOT_ALLOWED = 405

    /** Identical to the React Native sample's form. */
    val FORM_HTML = """<!doctype html>
<meta name="viewport" content="width=device-width, initial-scale=1">
<style>
  body { font: 15px -apple-system, Roboto, sans-serif; margin: 16px; color: #1F1E5D; }
  input, button { font-size: 16px; padding: 10px; width: 100%; box-sizing: border-box; margin-bottom: 10px; }
  button { background: #706CFF; color: #fff; border: 0; border-radius: 8px; font-weight: 700; }
  .note { color: #5A5D77; font-size: 13px; }
</style>
<h3>Payment form</h3>
<p class="note">Submits a POST. The endpoint rejects GET with 405, so a
replayed-as-GET reload is unmistakable.</p>
<form method="POST" action="$ECHO_URL">
  <input name="card" value="4111111111111111">
  <input name="amount" value="42.00">
  <button type="submit">Submit payment</button>
</form>"""

    /**
     * Submits the form through injected JavaScript. The WebView's DOM is not in the accessibility
     * tree, so a coordinate tap on the HTML button is unreliable and unautomatable; this lets a
     * test driver trigger the POST the same way a human would.
     */
    const val SUBMIT_SCRIPT = "document.forms[0].submit(); true;"

    const val STATUS_IDLE = "submit the form below"
    const val STATUS_SUBMITTING = "submitting…"
    const val STATUS_CAPTURING = "capturing layout…"

    fun navigations(count: Int) = "navigations: $count"

    fun httpError(statusCode: Int, url: String): String =
        "HTTP $statusCode on $url" + if (statusCode == METHOD_NOT_ALLOWED) " — POST was replayed as GET" else ""

    fun loadError(description: String) = "error: $description"

    /**
     * The line for a finished navigation. Only the echo page gets one — the form page loading is
     * not news — and none when that navigation already reported an error: WebView calls
     * onPageFinished for an error page too, which would otherwise overwrite the 405 this screen
     * exists to show.
     */
    fun loaded(url: String, navigationFailed: Boolean): String? =
        if (!navigationFailed && url.contains("/post")) "loaded $url — read \"method\" in the echo" else null

    /**
     * Captures the layout now, on purpose. The reload that replayed the POST as a GET only ran when
     * a layout capture fired while the POST result was on screen, and nothing triggers one here by
     * itself — without this the check passes on a broken build too.
     */
    fun captureLayout(activity: Activity): Boolean =
        Connect.logScreenLayout(activity, ScreenName.WEBVIEW_POST, 0)
}
