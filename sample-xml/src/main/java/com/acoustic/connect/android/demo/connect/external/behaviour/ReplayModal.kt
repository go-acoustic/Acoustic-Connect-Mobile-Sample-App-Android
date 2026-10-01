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
import android.app.Dialog
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import com.acoustic.connect.android.connectmod.Connect
import com.acoustic.connect.android.demo.connect.external.R
import com.google.android.material.textfield.TextInputEditText
import java.lang.ref.WeakReference
import com.acoustic.connect.android.demo.connect.external.shared.R as SharedR

/**
 * Session-replay reference for a modal.
 *
 * A [Dialog] does not render inline: it opens a window of its own, outside the activity's view
 * tree. That makes it the Android counterpart of React Native's `<Modal>` — the case where layout
 * capture has to find content that is not under the screen it is capturing. The opaque variant
 * covers the screen; the transparent one dims it and leaves it visible behind the sheet.
 *
 * The controls are deliberately varied — text, an input, two buttons and a status line — so a
 * replay reviewer can confirm each is individually inspectable, not just visible in a screenshot.
 * The status line echoes the input and the tap count, so an interaction that never registered can
 * be told apart from one the SDK failed to capture.
 */
object ReplayModal {

    /** The modal on screen, if any — held weakly so a destroyed activity's dialog is not kept. */
    private var current: WeakReference<Dialog>? = null

    fun show(activity: Activity, transparent: Boolean) {
        // A quick double tap would otherwise stack a second modal over the first.
        if (current?.get()?.isShowing == true) return

        val dialog = Dialog(activity, R.style.Demo_ModalDialog)
        dialog.setContentView(R.layout.dialog_replay_modal)
        val backdrop = if (transparent) R.color.dimmed_backdrop else R.color.brand_background
        dialog.findViewById<View>(R.id.modal_backdrop).setBackgroundColor(ContextCompat.getColor(activity, backdrop))

        val note = dialog.findViewById<TextInputEditText>(R.id.field_replay_modal_note)
        val status = dialog.findViewById<TextView>(R.id.txt_replay_modal_result)
        var actionCount = 0
        fun renderStatus() {
            val shown = note.text?.toString().orEmpty().ifEmpty { "—" }
            status.text = activity.getString(SharedR.string.showcase_modal_status, shown, actionCount)
        }
        renderStatus()

        note.doAfterTextChanged { renderStatus() }
        dialog.findViewById<View>(R.id.btn_replay_modal_action).setOnClickListener {
            actionCount += 1
            renderStatus()
        }
        dialog.findViewById<View>(R.id.btn_close_replay_modal).setOnClickListener { dialog.dismiss() }
        dialog.setOnDismissListener { if (current?.get() === dialog) current = null }

        // Captures the dialog's own layout once it is on screen; the activity's capture would not
        // see a separate window.
        Connect.logScreenLayoutSetOnShowListener(activity, dialog)
        current = WeakReference(dialog)
        dialog.show()
    }
}
