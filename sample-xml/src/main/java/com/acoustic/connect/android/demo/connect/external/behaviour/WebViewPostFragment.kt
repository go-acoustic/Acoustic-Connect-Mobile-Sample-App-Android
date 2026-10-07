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
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import com.acoustic.connect.android.demo.connect.external.R
import com.acoustic.connect.android.demo.connect.external.contract.ScreenName
import com.acoustic.connect.android.demo.connect.external.ui.ScreenFragment
import com.acoustic.connect.android.demo.connect.external.ui.onClick

/**
 * Verifies that the SDK's WebView capture does not turn a form POST into a GET. See [WebViewPost]
 * for the mechanism; this screen only hosts the form and reports what the WebView saw.
 */
class WebViewPostFragment : ScreenFragment(R.layout.fragment_webview_post) {

    override val screenName = ScreenName.WEBVIEW_POST

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ScenarioCardBinder.bind(view.findViewById(R.id.card_webview_post), Scenarios.WEBVIEW_POST_NOT_REPLAYED_AS_GET)

        // Counts navigations of this view's WebView, so it lives and dies with the view. It is not
        // saved: a recreated view brings a new WebView that loads the form afresh, and a restored
        // count would describe the old one's page. The Compose screen behaves the same way.
        var navigations = 0
        // Set when the current navigation reports an error, so its onPageFinished keeps the line.
        var navigationFailed = false
        val status = view.findViewById<TextView>(R.id.webview_status)
        val navigationsLine = view.findViewById<TextView>(R.id.webview_navigations)
        fun render(text: String? = null) {
            text?.let { status.text = it }
            navigationsLine.text = WebViewPost.navigations(navigations)
        }
        render(WebViewPost.STATUS_IDLE)

        val webView = view.findViewById<WebView>(R.id.webview_post)
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = object : WebViewClient() {
            // Every committed navigation is counted, error pages included: a capture-driven
            // reload that ends on the 405 is exactly the extra one this count exists to show.
            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                navigationFailed = false
            }

            override fun onPageFinished(view: WebView, url: String) {
                navigations += 1
                render(WebViewPost.loaded(url, navigationFailed))
            }

            override fun onReceivedHttpError(
                view: WebView,
                request: WebResourceRequest,
                errorResponse: WebResourceResponse,
            ) {
                if (request.isForMainFrame) {
                    navigationFailed = true
                    render(WebViewPost.httpError(errorResponse.statusCode, request.url.toString()))
                }
            }

            override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
                if (request.isForMainFrame) {
                    navigationFailed = true
                    render(WebViewPost.loadError(error.description.toString()))
                }
            }
        }
        webView.loadForm()

        view.onClick(R.id.btn_webview_submit) {
            render(WebViewPost.STATUS_SUBMITTING)
            webView.evaluateJavascript(WebViewPost.SUBMIT_SCRIPT, null)
        }
        view.onClick(R.id.btn_webview_capture) {
            render(WebViewPost.STATUS_CAPTURING)
            WebViewPost.captureLayout(requireActivity())
        }
        view.onClick(R.id.btn_webview_reset) {
            navigations = 0
            render(WebViewPost.STATUS_IDLE)
            webView.loadForm()
        }
    }

    private fun WebView.loadForm() {
        loadDataWithBaseURL(WebViewPost.BASE_URL, WebViewPost.FORM_HTML, "text/html", "utf-8", null)
    }
}
