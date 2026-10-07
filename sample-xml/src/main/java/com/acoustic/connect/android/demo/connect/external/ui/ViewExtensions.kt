/*
 * Copyright (C) 2026 Acoustic, L.P. All rights reserved.
 *
 * Licensed under the Acoustic License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy
 * at https://www.acoustic.com/licenses/acoustic-license
 *
 * Sample app provided "as is", without warranty of any kind.
 */
package com.acoustic.connect.android.demo.connect.external.ui

import android.view.View
import android.widget.TextView
import androidx.annotation.IdRes

/** Runs [action] when the view [id] under this one is clicked. */
fun View.onClick(@IdRes id: Int, action: () -> Unit) {
    findViewById<View>(id).setOnClickListener { action() }
}

/**
 * Shows [text] in the result line [id]. Result lines start hidden and appear once their card has
 * been driven, as in React Native.
 */
fun View.showResult(@IdRes id: Int, text: String) {
    findViewById<TextView>(id).apply {
        this.text = text
        visibility = View.VISIBLE
    }
}
