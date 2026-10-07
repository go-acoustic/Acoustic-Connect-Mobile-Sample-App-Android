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

import com.acoustic.connect.android.connectmod.Connect

/**
 * The SDK calls behind the Verification-only cards, and the result line each prints. Cards the
 * Verification screen shares with the Showcase use [ShowcaseActions].
 */
object VerificationActions {

    /**
     * Logs an identity with the signal type and parameters left to the SDK's defaults — the path
     * the identity-defaults scenario exists to exercise. On React Native the bridge supplies them;
     * natively the SDK does, and it defaults to `loggedIn` with no method attribute.
     */
    fun logIdentityDefaulted(): String {
        val ok = Connect.logIdentificationEvent("Email", "defaults@example.com")
        return "${mark(ok)} defaulted — expect signalType: loggedIn"
    }

    /** The contrast case: an explicit `accountRegistered`, which legitimately uses registrationMethod. */
    fun logIdentityExplicit(): String {
        val ok = Connect.logIdentificationEvent(
            identifierName = "Email",
            identifierValue = "explicit@example.com",
            signalType = "accountRegistered",
            additionalParameters = mapOf("registrationMethod" to "email"),
        )
        return "${mark(ok)} explicit — expect registrationMethod: email"
    }

    private fun mark(ok: Boolean) = if (ok) "✓" else "✗"
}
