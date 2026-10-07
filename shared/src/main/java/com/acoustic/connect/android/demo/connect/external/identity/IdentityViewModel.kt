/*
 * Copyright (C) 2026 Acoustic, L.P. All rights reserved.
 *
 * Licensed under the Acoustic License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy
 * at https://www.acoustic.com/licenses/acoustic-license
 *
 * Sample app provided "as is", without warranty of any kind.
 */
package com.acoustic.connect.android.demo.connect.external.identity

import android.app.Application
import android.content.Context
import android.util.Base64
import androidx.lifecycle.AndroidViewModel
import com.acoustic.connect.android.connectmod.Connect
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

private const val PREFS_NAME = "identity_prefs"
private const val KEY_HISTORY = "identity_history"
private const val HISTORY_SEPARATOR = "|||"
private const val MAX_HISTORY = 5
private const val SIGNAL_LOGGED_IN = "loggedIn"
private const val SIGNAL_ACCOUNT_REGISTERED = "accountRegistered"
private const val METHOD_EMAIL = "email"

data class IdentityHistoryEntry(val name: String, val value: String)

data class IdentityUiState(
    val identifierName: String = "",
    val identifierValue: String = "",
    /** The Last Result line — `✓ Email: user@example.com`, or null before the first call. */
    val lastResult: String? = null,
    val history: List<IdentityHistoryEntry> = emptyList(),
) {
    /** Both buttons stay disabled until each field holds something other than whitespace. */
    val canLog: Boolean get() = identifierName.isNotBlank() && identifierValue.isNotBlank()
}

/**
 * The Identity tab's state and its two identity signals, shared by both samples.
 *
 * `loggedIn` pairs with `loginMethod` and `accountRegistered` with `registrationMethod`, the same
 * payloads the React Native and iOS samples send, so one e2e assertion holds for all of them.
 */
class IdentityViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(IdentityUiState(history = loadHistory()))
    val uiState: StateFlow<IdentityUiState> = _uiState.asStateFlow()

    fun onIdentifierNameChanged(value: String) {
        _uiState.update { it.copy(identifierName = value) }
    }

    fun onIdentifierValueChanged(value: String) {
        _uiState.update { it.copy(identifierValue = value) }
    }

    fun onLogLoggedIn() = logIdentity(SIGNAL_LOGGED_IN, mapOf("loginMethod" to METHOD_EMAIL))

    fun onLogAccountRegistered() =
        logIdentity(SIGNAL_ACCOUNT_REGISTERED, mapOf("registrationMethod" to METHOD_EMAIL))

    fun onHistoryEntrySelected(entry: IdentityHistoryEntry) {
        _uiState.update { it.copy(identifierName = entry.name, identifierValue = entry.value) }
    }

    private fun logIdentity(signalType: String, parameters: Map<String, String>) {
        val name = _uiState.value.identifierName.trim()
        val value = _uiState.value.identifierValue.trim()
        if (name.isEmpty() || value.isEmpty()) return

        val success = Connect.logIdentificationEvent(
            identifierName = name,
            identifierValue = value,
            signalType = signalType,
            additionalParameters = parameters,
        )
        val updated = buildUpdatedHistory(_uiState.value.history, name, value)
        saveHistory(updated)
        _uiState.update {
            it.copy(
                lastResult = if (success) "✓ $name: $value" else "✗ Failed to log $name",
                history = updated,
            )
        }
    }

    private fun saveHistory(history: List<IdentityHistoryEntry>) {
        // Name and value are Base64-encoded before joining so neither can ever contain the
        // separator itself: a plain-text join broke whenever a user typed HISTORY_SEPARATOR
        // into either field, truncating the name and corrupting the value on the next load.
        val serialised = history.joinToString("\n") { encodeEntry(it) }
        prefs.edit().putString(KEY_HISTORY, serialised).apply()
    }

    private fun loadHistory(): List<IdentityHistoryEntry> {
        val raw = prefs.getString(KEY_HISTORY, null) ?: return emptyList()
        return raw.lines().mapNotNull { decodeEntry(it) }
    }

    private fun encodeEntry(entry: IdentityHistoryEntry): String {
        val name = Base64.encodeToString(entry.name.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
        val value = Base64.encodeToString(entry.value.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
        return "$name$HISTORY_SEPARATOR$value"
    }

    private fun decodeEntry(line: String): IdentityHistoryEntry? {
        val parts = line.split(HISTORY_SEPARATOR, limit = 2)
        if (parts.size != 2) return null
        return try {
            val name = String(Base64.decode(parts[0], Base64.NO_WRAP), Charsets.UTF_8)
            val value = String(Base64.decode(parts[1], Base64.NO_WRAP), Charsets.UTF_8)
            IdentityHistoryEntry(name, value)
        } catch (e: IllegalArgumentException) {
            null // A pre-fix plain-text entry left over from an older install; drop it rather than
                 // show mangled text.
        }
    }
}

/**
 * Newest first, at most five, one entry per identifier name: logging a name again replaces its
 * older value rather than listing both.
 */
internal fun buildUpdatedHistory(
    history: List<IdentityHistoryEntry>,
    name: String,
    value: String,
): List<IdentityHistoryEntry> =
    (listOf(IdentityHistoryEntry(name, value)) + history.filter { it.name != name }).take(MAX_HISTORY)
