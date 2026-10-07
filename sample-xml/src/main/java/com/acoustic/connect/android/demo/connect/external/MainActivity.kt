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

import android.os.Bundle
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.acoustic.connect.android.connectmod.Connect
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * Entry point of the analytics-only sample.
 *
 * <p>This app deliberately depends on `io.github.go-acoustic:connect` rather than `connect-push`:
 * push is independent of the UI framework, so a second copy of it would only duplicate the
 * certificates, service registrations and Firebase/AGConnect config that the Compose sample
 * already demonstrates. What genuinely differs between Views and Compose is the analytics
 * surface — screen-view emission, control capture and masking all take different routes — and
 * that is what this app exists to cover.
 *
 * <p>`Connect.enable` is called without a `pushConfig`, which is the supported analytics-only
 * integration path.
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val credentials = AcousticCredentials.load(this)

        Connect.init(application)
        Connect.enable(
            appKey = credentials.appKey,
            postMessageUrl = credentials.collectorUrl,
        )

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // The two tab roots show no back arrow; everything stacked on the Behaviour hub does.
        val topLevel = AppBarConfiguration(setOf(R.id.tab_identity, R.id.behaviourFragment))
        findViewById<MaterialToolbar>(R.id.toolbar).setupWithNavController(navController, topLevel)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNav.setupWithNavController(navController)
        // Like React Navigation's bottom tabs: pressing the tab you are on pops it to its root.
        bottomNav.setOnItemReselectedListener { item ->
            if (item.itemId == R.id.tab_behaviour) navController.popBackStack(R.id.behaviourFragment, false)
        }
    }

    override fun dispatchTouchEvent(e: MotionEvent?): Boolean {
        Connect.dispatchTouchEvent(this, e)
        return super.dispatchTouchEvent(e)
    }
}
