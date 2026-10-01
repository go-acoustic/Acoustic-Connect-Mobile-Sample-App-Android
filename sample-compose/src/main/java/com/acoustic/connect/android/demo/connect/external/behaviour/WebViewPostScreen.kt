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

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Bitmap
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.acoustic.connect.android.demo.connect.external.analytics.ScreenviewUnloadEffect
import com.acoustic.connect.android.demo.connect.external.contract.SampleId
import com.acoustic.connect.android.demo.connect.external.contract.ScreenName
import com.acoustic.connect.android.demo.connect.external.ui.components.DemoCard
import com.acoustic.connect.android.demo.connect.external.ui.components.DemoScreen
import com.acoustic.connect.android.demo.connect.external.ui.components.HintText
import com.acoustic.connect.android.demo.connect.external.ui.components.MonoText
import com.acoustic.connect.android.demo.connect.external.ui.components.NoteBox
import com.acoustic.connect.android.demo.connect.external.ui.components.PrimaryButton
import com.acoustic.connect.android.demo.connect.external.ui.components.SecondaryButton
import com.acoustic.connect.android.demo.connect.external.ui.components.contractTag
import com.acoustic.connect.android.demo.connect.external.shared.R as SharedR

private val WEBVIEW_HEIGHT = 420.dp

/**
 * Verifies that the SDK's WebView capture does not turn a form POST into a GET. See [WebViewPost]
 * for the mechanism; this screen only hosts the form and reports what the WebView saw.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewPostScreen() {
    val activity = LocalContext.current as? Activity
    var status by remember { mutableStateOf<String?>(null) }
    var navigations by remember { mutableIntStateOf(0) }
    // Set when the current navigation reports an error, so its onPageFinished keeps the line.
    var navigationFailed by remember { mutableStateOf(false) }
    var webView by remember { mutableStateOf<WebView?>(null) }

    ScreenviewUnloadEffect(ScreenName.WEBVIEW_POST)

    DemoScreen {
        ScenarioCard(Scenarios.WEBVIEW_POST_NOT_REPLAYED_AS_GET) {
            NoteBox {
                MonoText(status ?: WebViewPost.STATUS_IDLE)
                MonoText(WebViewPost.navigations(navigations))
            }
            PrimaryButton(
                title = stringResource(SharedR.string.webview_submit),
                tag = SampleId.BTN_WEBVIEW_SUBMIT,
                onClick = {
                    status = WebViewPost.STATUS_SUBMITTING
                    webView?.evaluateJavascript(WebViewPost.SUBMIT_SCRIPT, null)
                },
            )
            SecondaryButton(
                title = stringResource(SharedR.string.webview_capture),
                tag = SampleId.BTN_WEBVIEW_CAPTURE,
                onClick = {
                    status = WebViewPost.STATUS_CAPTURING
                    activity?.let(WebViewPost::captureLayout)
                },
            )
            HintText(stringResource(SharedR.string.webview_capture_hint))
            SecondaryButton(
                title = stringResource(SharedR.string.webview_reset),
                tag = SampleId.BTN_WEBVIEW_RESET,
                onClick = {
                    status = null
                    navigations = 0
                    webView?.loadForm()
                },
            )
        }

        DemoCard(title = stringResource(SharedR.string.webview_card_title)) {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(WEBVIEW_HEIGHT)
                    .contractTag(SampleId.WEBVIEW_POST),
                factory = { context ->
                    WebView(context).apply {
                        settings.javaScriptEnabled = true
                        webViewClient = object : WebViewClient() {
                            // Every committed navigation is counted, error pages included: a
                            // capture-driven reload that ends on the 405 is exactly the extra one
                            // this count exists to show.
                            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                                navigationFailed = false
                            }

                            override fun onPageFinished(view: WebView, url: String) {
                                navigations += 1
                                WebViewPost.loaded(url, navigationFailed)?.let { status = it }
                            }

                            override fun onReceivedHttpError(
                                view: WebView,
                                request: WebResourceRequest,
                                errorResponse: WebResourceResponse,
                            ) {
                                if (request.isForMainFrame) {
                                    navigationFailed = true
                                    status = WebViewPost.httpError(errorResponse.statusCode, request.url.toString())
                                }
                            }

                            override fun onReceivedError(
                                view: WebView,
                                request: WebResourceRequest,
                                error: WebResourceError,
                            ) {
                                if (request.isForMainFrame) {
                                    navigationFailed = true
                                    status = WebViewPost.loadError(error.description.toString())
                                }
                            }
                        }
                        loadForm()
                        webView = this
                    }
                },
            )
        }
    }
}

private fun WebView.loadForm() {
    loadDataWithBaseURL(WebViewPost.BASE_URL, WebViewPost.FORM_HTML, "text/html", "utf-8", null)
}
