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

import com.acoustic.connect.android.demo.connect.external.R

/**
 * The contract id of each Screen Views button, by case id. The buttons are built in code, so their
 * ids are declared in `ids.xml` and looked up here; a unit test holds both maps to the shared
 * matrix, so a case added there without an id fails the build rather than a device run.
 */
object ScreenViewButtons {

    /** `btn_navigate_<case>`, one per navigation case. */
    val navigate: Map<String, Int> = mapOf(
        "catalog" to R.id.btn_navigate_catalog,
        "product_details" to R.id.btn_navigate_product_details,
        "bid_confirmation" to R.id.btn_navigate_bid_confirmation,
        "checkout" to R.id.btn_navigate_checkout,
        "reserved_chars" to R.id.btn_navigate_reserved_chars,
        "non_ascii" to R.id.btn_navigate_non_ascii,
        "long" to R.id.btn_navigate_long,
        "url_shaped" to R.id.btn_navigate_url_shaped,
        "whitespace" to R.id.btn_navigate_whitespace,
    )

    /** `btn_screenview_<case>`, one per direct case. */
    val direct: Map<String, Int> = mapOf(
        "reserved_chars" to R.id.btn_screenview_reserved_chars,
        "non_ascii" to R.id.btn_screenview_non_ascii,
        "long" to R.id.btn_screenview_long,
        "url_shaped" to R.id.btn_screenview_url_shaped,
        "whitespace" to R.id.btn_screenview_whitespace,
        "empty" to R.id.btn_screenview_empty,
        "null" to R.id.btn_screenview_null,
    )
}
