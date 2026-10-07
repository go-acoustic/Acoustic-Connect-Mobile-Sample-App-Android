/*
 * Copyright (C) 2026 Acoustic, L.P. All rights reserved.
 *
 * Licensed under the Acoustic License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy
 * at https://www.acoustic.com/licenses/acoustic-license
 *
 * Sample app provided "as is", without warranty of any kind.
 */

package com.acoustic.connect.android.demo.connect.external.gestures

import kotlin.math.abs

/**
 * Direction label for a completed drag, or null when neither axis cleared [thresholdPx] — a short
 * drag that ends near where it began is not a swipe. The dominant axis wins, so a diagonal still
 * reports one direction.
 *
 * <p>Takes plain floats rather than a framework point type so both samples call the same function:
 * the labels this returns end up in the signal payloads the two apps are compared on, and a second
 * copy of this `when` is a second chance for those payloads to disagree for a reason that has
 * nothing to do with the SDK.
 */
fun swipeName(travelX: Float, travelY: Float, thresholdPx: Float): String? = when {
    abs(travelX) < thresholdPx && abs(travelY) < thresholdPx -> null
    abs(travelX) >= abs(travelY) -> if (travelX > 0) "swipeRight" else "swipeLeft"
    else -> if (travelY > 0) "swipeDown" else "swipeUp"
}
