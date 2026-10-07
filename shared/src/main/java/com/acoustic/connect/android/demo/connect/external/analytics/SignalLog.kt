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

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

private const val MAX_ENTRIES = 50

/**
 * In-memory record of the analytics calls this app makes, newest first.
 *
 * <p>It exists so a lifecycle transition can be observed on the device instead of only in a
 * collector payload — the app-state signals in the analytics audit (foreground, background, orientation)
 * are awkward to verify otherwise, because the transition that produces them also takes the app
 * off screen.
 *
 * <p>Scope note: this records what the *app* asked the SDK to log and whether the SDK accepted the
 * call. It says nothing about what the SDK auto-instruments or what actually reaches the collector,
 * so it narrows the payload comparison rather than replacing it.
 */
object SignalLog {

    /**
     * @param accepted the SDK's return value for the call, or null where the call was not made
     *   (for example the SDK was not enabled yet).
     */
    data class Entry(
        val timestampMillis: Long,
        val name: String,
        val detail: String,
        val accepted: Boolean?,
    )

    private val _entries = MutableStateFlow<List<Entry>>(emptyList())
    val entries: StateFlow<List<Entry>> = _entries.asStateFlow()

    fun record(name: String, detail: String = "", accepted: Boolean? = null) {
        val entry = Entry(System.currentTimeMillis(), name, detail, accepted)
        _entries.update { current -> (listOf(entry) + current).take(MAX_ENTRIES) }
    }

    fun clear() {
        _entries.value = emptyList()
    }
}
