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
 * Screen-name test matrix for the inferred `pageView` signal. Reserved: the Screen views screen
 * that renders it comes later.
 *
 * The platform derives a `pageView` signal from every screenview message. That inference was
 * written for web, where the message carries a URL; a mobile screen view has none, so the
 * server-side rule falls back to the screen name. That makes the screen name the input under test,
 * and this table drives a spread of name shapes. [ScreenViewCase.name] is the exact string expected
 * to arrive as the screenview name. Every name is byte-identical to the React Native and iOS
 * samples.
 *
 * Two delivery paths, because they are not interchangeable:
 *
 * - [CaseVia.NAV] navigates to a screen that logs the name — the path a real app takes. It cannot
 *   carry a blank name.
 * - [CaseVia.DIRECT] calls `Connect.logScreenview` with the exact string — the only way to put the
 *   empty and null names on the wire.
 */
enum class CaseVia { NAV, DIRECT, BOTH }

data class ScreenViewCase(
    /** Stable id — also the id suffix, so automation can address a case. */
    val id: String,
    /** Button label. */
    val label: String,
    /**
     * The screen name to log. `null` is deliberate: Android stringifies a null logical page name,
     * so it should arrive as the literal text "null" rather than as absent.
     */
    val name: String?,
    /** What this case probes on the server side. */
    val probes: String,
    val via: CaseVia,
)

object ScreenViewCases {

    private const val LONG_NAME_LENGTH = 300

    /** Exactly 300 characters, to probe any maximum-length bound on the url attribute. */
    val LONG_NAME: String = "Lot 4815 ".repeat(34).take(LONG_NAME_LENGTH)

    /**
     * Plausible drill-down screens for an auction or retail app — the happy path: ordinary names,
     * some with a space, repeated as you navigate back and forth.
     */
    val REALISTIC: List<ScreenViewCase> = listOf(
        ScreenViewCase(
            id = "catalog",
            label = "Catalog",
            name = "Catalog",
            probes = "Baseline. A bare word is not a URL — proves the schema accepts a non-URL " +
                "string in url at all.",
            via = CaseVia.NAV,
        ),
        ScreenViewCase(
            id = "product_details",
            label = "Product Details",
            name = "Product Details",
            probes = "Space in the name. Most common real-world shape; a naive URL normaliser may " +
                "reject or escape it.",
            via = CaseVia.NAV,
        ),
        ScreenViewCase(
            id = "bid_confirmation",
            label = "Bid Confirmation",
            name = "Bid Confirmation",
            probes = "Second spaced name, so repeat vs distinct url values can be told apart in " +
                "Signal Management.",
            via = CaseVia.NAV,
        ),
        ScreenViewCase(
            id = "checkout",
            label = "Checkout",
            name = "Checkout",
            probes = "Terminal screen. Visited more than once via back-navigation, to check repeat " +
                "urls all stay valid.",
            via = CaseVia.NAV,
        ),
    )

    /**
     * Name shapes that could still defeat the fallback. Ugly on purpose; the blank and null cases
     * are the two most likely to still be rejected.
     */
    val EDGE: List<ScreenViewCase> = listOf(
        ScreenViewCase(
            id = "reserved_chars",
            label = "URL-reserved characters",
            name = "Order #4815 & Refund?ref=a/b",
            probes = "Contains ? & # / — if the rule parses the value as a URL, the query/fragment " +
                "split may truncate or reject it.",
            via = CaseVia.BOTH,
        ),
        ScreenViewCase(
            id = "non_ascii",
            label = "Non-ASCII + emoji",
            name = "Płatności ✓ 🛒",
            probes = "Multi-byte characters. Probes encoding through the collector, the rule, and " +
                "the schema validator.",
            via = CaseVia.BOTH,
        ),
        ScreenViewCase(
            id = "long",
            label = "300-character name",
            name = LONG_NAME,
            probes = "Any max-length bound on the url attribute. A truncation would show as a " +
                "clipped url; a bound would show as invalid.",
            via = CaseVia.BOTH,
        ),
        ScreenViewCase(
            id = "url_shaped",
            label = "Already URL-shaped",
            name = "https://app.example.com/looks-like-a-url",
            probes = "Name that is already a URL — confirms the rule does not prefix or wrap a " +
                "value that needs no fallback treatment.",
            via = CaseVia.BOTH,
        ),
        ScreenViewCase(
            id = "whitespace",
            label = "Whitespace only",
            name = "   ",
            probes = "Non-empty but semantically blank. Passes a null check, so it may produce a " +
                "valid-but-useless url.",
            via = CaseVia.BOTH,
        ),
        ScreenViewCase(
            id = "empty",
            label = "Empty name",
            name = "",
            probes = "The highest-risk case. A framework can map an undefined route name to '', " +
                "and an empty url may still fail the required-field check.",
            via = CaseVia.DIRECT,
        ),
        ScreenViewCase(
            id = "null",
            label = "Null name",
            name = null,
            probes = "Android stringifies a null name, so url should read literally \"null\". iOS " +
                "drops the message instead — a platform difference worth recording.",
            via = CaseVia.DIRECT,
        ),
    )

    val ALL: List<ScreenViewCase> = REALISTIC + EDGE

    /** Cases reachable by navigating to a screen that logs the name. */
    val NAV: List<ScreenViewCase> = ALL.filter { it.via == CaseVia.NAV || it.via == CaseVia.BOTH }

    /** Cases that must go through the direct call to keep the exact string. */
    val DIRECT: List<ScreenViewCase> = ALL.filter { it.via == CaseVia.DIRECT || it.via == CaseVia.BOTH }

    fun byId(id: String): ScreenViewCase? = ALL.firstOrNull { it.id == id }
}

private const val MAX_SHOWN_LENGTH = 48
private const val SHOWN_PREFIX_LENGTH = 45

/** Renders a name so a blank or null one is visible rather than invisible. */
fun describeName(name: String?): String = when {
    name == null -> "(null)"
    name.isEmpty() -> "(empty string)"
    name.isBlank() -> "(whitespace ×${name.length})"
    name.length > MAX_SHOWN_LENGTH -> "${name.take(SHOWN_PREFIX_LENGTH)}… (${name.length} chars)"
    else -> name
}
