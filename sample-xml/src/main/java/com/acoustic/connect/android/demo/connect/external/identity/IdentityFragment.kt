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

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.acoustic.connect.android.connectmod.Connect
import com.acoustic.connect.android.demo.connect.external.R
import com.acoustic.connect.android.demo.connect.external.contract.ScreenName
import com.acoustic.connect.android.demo.connect.external.ui.ScreenFragment
import kotlinx.coroutines.launch
import com.acoustic.connect.android.demo.connect.external.shared.R as SharedR

/**
 * Identity tab. Logs a `loggedIn` or `accountRegistered` signal, shows the last result, and lists
 * the five most recent identifier pairs so a known-good combination can be re-sent quickly.
 */
class IdentityFragment : ScreenFragment(R.layout.fragment_identity) {

    override val screenName = ScreenName.IDENTITY
    override val headerTitle get() = getString(SharedR.string.identity_header)

    private val viewModel: IdentityViewModel by viewModels()

    private lateinit var nameField: EditText
    private lateinit var valueField: EditText
    private lateinit var loggedInButton: Button
    private lateinit var registeredButton: Button
    private lateinit var lastResultCard: View
    private lateinit var lastResult: TextView
    private lateinit var historyCard: View
    private lateinit var historyContainer: LinearLayout

    private var isUpdatingFromViewModel = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        nameField = view.findViewById(R.id.et_identifier_name)
        valueField = view.findViewById(R.id.et_identifier_value)
        loggedInButton = view.findViewById(R.id.btn_send_identity_signal)
        registeredButton = view.findViewById(R.id.btn_send_account_registered_signal)
        lastResultCard = view.findViewById(R.id.card_last_result)
        lastResult = view.findViewById(R.id.tv_identity_status_message)
        historyCard = view.findViewById(R.id.card_history)
        historyContainer = view.findViewById(R.id.ll_history_container)

        nameField.doAfterTextChanged { text ->
            if (!isUpdatingFromViewModel) viewModel.onIdentifierNameChanged(text?.toString().orEmpty())
        }
        valueField.doAfterTextChanged { text ->
            if (!isUpdatingFromViewModel) viewModel.onIdentifierValueChanged(text?.toString().orEmpty())
        }
        loggedInButton.setOnClickListener { viewModel.onLogLoggedIn() }
        registeredButton.setOnClickListener { viewModel.onLogAccountRegistered() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state -> render(state) }
            }
        }
    }

    private fun render(state: IdentityUiState) {
        isUpdatingFromViewModel = true
        if (nameField.text?.toString() != state.identifierName) {
            nameField.setText(state.identifierName)
            nameField.setSelection(state.identifierName.length)
        }
        if (valueField.text?.toString() != state.identifierValue) {
            valueField.setText(state.identifierValue)
            valueField.setSelection(state.identifierValue.length)
        }
        isUpdatingFromViewModel = false

        loggedInButton.isEnabled = state.canLog
        registeredButton.isEnabled = state.canLog

        lastResultCard.visibility = if (state.lastResult == null) View.GONE else View.VISIBLE
        lastResult.text = state.lastResult

        historyCard.visibility = if (state.history.isEmpty()) View.GONE else View.VISIBLE
        rebuildHistory(state.history)
    }

    private fun rebuildHistory(history: List<IdentityHistoryEntry>) {
        historyContainer.removeAllViews()
        history.forEachIndexed { index, entry ->
            val itemView = layoutInflater.inflate(R.layout.item_history_entry, historyContainer, false)
            itemView.findViewById<View>(R.id.divider).visibility = if (index > 0) View.VISIBLE else View.GONE
            itemView.findViewById<TextView>(R.id.tv_history_name).text = entry.name
            itemView.findViewById<TextView>(R.id.tv_history_value).text = entry.value
            itemView.setOnClickListener {
                Connect.logEvent(it, "OnHistoryItemClick")
                viewModel.onHistoryEntrySelected(entry)
            }
            historyContainer.addView(itemView)
        }
    }
}
