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

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** The WebView POST status lines, which a UI test reads back in both samples. */
class WebViewPostTest {

    @Test
    fun `a 405 on the echo says the POST was replayed as a GET`() {
        assertEquals(
            "HTTP 405 on https://httpbin.org/post — POST was replayed as GET",
            WebViewPost.httpError(405, WebViewPost.ECHO_URL),
        )
    }

    @Test
    fun `any other HTTP error is reported plainly`() {
        assertEquals("HTTP 502 on https://httpbin.org/post", WebViewPost.httpError(502, WebViewPost.ECHO_URL))
    }

    @Test
    fun `only the echo page produces a loaded line`() {
        assertNull(WebViewPost.loaded("${WebViewPost.BASE_URL}/", navigationFailed = false))
        assertEquals(
            "loaded https://httpbin.org/post — read \"method\" in the echo",
            WebViewPost.loaded(WebViewPost.ECHO_URL, navigationFailed = false),
        )
    }

    @Test
    fun `a failed navigation keeps its error line`() {
        // WebView calls onPageFinished for the 405 page too; that must not hide the 405.
        assertNull(WebViewPost.loaded(WebViewPost.ECHO_URL, navigationFailed = true))
    }

    @Test
    fun `the form posts to the echo URL`() {
        assertTrue(WebViewPost.FORM_HTML.contains("method=\"post\"", ignoreCase = true))
        assertTrue(WebViewPost.FORM_HTML.contains("action=\"${WebViewPost.ECHO_URL}\""))
    }
}
