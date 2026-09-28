/*
 * Copyright (C) 2026 Acoustic, L.P. All rights reserved.
 *
 * Licensed under the Acoustic License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy
 * at https://www.acoustic.com/licenses/acoustic-license
 *
 * Sample app provided "as is", without warranty of any kind.
 */
package com.acoustic.connect.android.demo.connect.external

import android.content.Context
import java.util.Properties

object AcousticCredentials {
    private const val CONFIG_ASSET = "ConnectBasicConfig.properties"
    private const val KEY_APP_KEY = "AppKey"
    private const val KEY_POST_URL = "PostMessageUrl"

    fun load(context: Context): Credentials {
        val props = Properties().apply {
            context.assets.open(CONFIG_ASSET).use { load(it) }
        }
        return Credentials(
            appKey = props.getProperty(KEY_APP_KEY).orEmpty(),
            collectorUrl = props.getProperty(KEY_POST_URL).orEmpty(),
        )
    }

    data class Credentials(val appKey: String, val collectorUrl: String)
}
