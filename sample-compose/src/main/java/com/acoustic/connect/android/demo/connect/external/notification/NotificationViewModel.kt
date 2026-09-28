/*
 * Copyright (C) 2026 Acoustic, L.P. All rights reserved.
 *
 * Licensed under the Acoustic License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy
 * at https://www.acoustic.com/licenses/acoustic-license
 *
 * Sample app provided "as is", without warranty of any kind.
 */
package com.acoustic.connect.android.demo.connect.external.notification

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.AndroidViewModel
import com.acoustic.connect.android.connectmod.Connect
import com.acoustic.connect.android.connectmod.push.model.Provider
import com.acoustic.connect.android.connectmod.push.model.Token
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class NotificationViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    init {
        refreshAuthorization()
    }

    fun refreshAuthorization() {
        val cachedToken = prefs.getString(KEY_LAST_TOKEN, "").orEmpty()
        val osPermitted = areNotificationsPermitted()
        val authorized = osPermitted && cachedToken.isNotBlank()
        _uiState.update {
            it.copy(
                isNotificationAuthorized = authorized,
                pushToken = if (it.pushToken.isBlank()) cachedToken else it.pushToken,
                tokenStatus = when {
                    it.tokenStatus is TokenStatus.Success -> it.tokenStatus
                    cachedToken.isNotBlank() -> TokenStatus.Success(cachedToken)
                    else -> it.tokenStatus
                },
            )
        }
    }

    fun onNotificationPermissionResult(isGranted: Boolean) {
        Log.d(TAG, "POST_NOTIFICATIONS permission granted: $isGranted")
        if (isGranted) {
            try {
                Connect.push.turnOnPush()
                fetchToken()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to turn on push: ${e.message}")
                Connect.logExceptionEvent(
                    e.javaClass.simpleName,
                    e.message,
                    Log.getStackTraceString(e),
                    false,
                )
                _uiState.update { it.copy(notificationStatusMessage = "Error: failed to enable push notifications", isError = true) }
            }
        } else {
            prefs.edit().remove(KEY_LAST_TOKEN).apply()
            _uiState.update {
                it.copy(
                    isNotificationAuthorized = false,
                    notificationStatusMessage = "Error: push notifications permission denied",
                    isError = true,
                )
            }
        }
    }

    private fun areNotificationsPermitted(): Boolean =
        NotificationManagerCompat.from(getApplication()).areNotificationsEnabled()

    fun setToken(token: Token) {
        val tokenString = token.token
        if (tokenString.isBlank()) return
        prefs.edit().putString(KEY_LAST_TOKEN, tokenString).apply()
        _uiState.update {
            it.copy(
                pushToken = tokenString,
                pushProvider = token.provider,
                isNotificationAuthorized = areNotificationsPermitted(),
                tokenStatus = TokenStatus.Success(tokenString),
                notificationStatusMessage = "",
                isError = false,
            )
        }
    }

    private fun fetchToken() {
        _uiState.update { it.copy(tokenStatus = TokenStatus.Loading) }
        Connect.push.getToken()
            .addOnSuccessListener { token ->
                val tokenString = token.token
                val provider = token.provider
                Log.d(TAG, "Token received: $tokenString")
                if (tokenString.isNotBlank()) {
                    prefs.edit().putString(KEY_LAST_TOKEN, tokenString).apply()
                }
                _uiState.update {
                    it.copy(
                        isNotificationAuthorized = areNotificationsPermitted() && tokenString.isNotBlank(),
                        tokenStatus = TokenStatus.Success(tokenString),
                        notificationStatusMessage = "",
                        isError = false,
                        pushToken = tokenString,
                        pushProvider = provider,
                    )
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to get token: ${e.message}")
                Connect.logExceptionEvent(
                    e.javaClass.simpleName,
                    e.message,
                    Log.getStackTraceString(e),
                    false,
                )
                _uiState.update {
                    it.copy(
                        isNotificationAuthorized = false,
                        tokenStatus = TokenStatus.Failure(
                            e.message ?: "Unknown error"
                        )
                    )
                }
            }
    }

    companion object {
        private const val TAG = "ConnectDemo"
        private const val PREFS_NAME = "notification_prefs"
        private const val KEY_LAST_TOKEN = "last_push_token"
    }
}

data class NotificationUiState(
    val tokenStatus: TokenStatus = TokenStatus.Idle,
    val isNotificationAuthorized: Boolean = false,
    val notificationStatusMessage: String = "",
    // Drives the status-message color. Not derived from notificationStatusMessage's text —
    // a prior version matched on "startsWith("Error")" and silently rendered any future
    // error message green if its wording ever changed.
    val isError: Boolean = false,
    val pushToken: String = "",
    val pushProvider: Provider? = null,
    val statusMessage: String = "Connect SDK is enabled automatically",
    val isSdkEnabled: Boolean = true,
)

sealed class TokenStatus {
    object Idle : TokenStatus()
    object Loading : TokenStatus()
    data class Success(val token: String) : TokenStatus()
    data class Failure(val message: String) : TokenStatus()
}
