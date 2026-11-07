// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 Edgard
package br.dev.edgard.easygamepad

import android.os.Bundle
import android.annotation.SuppressLint
import android.view.KeyEvent
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import br.dev.edgard.easygamepad.ui.HubApp

class MainActivity : ComponentActivity() {
    private val model: HubViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { HubApp(model) }
    }
    // Override the public Activity input hook. Core's inherited restriction is
    // overly broad here; gamepad testing must intercept events before UI focus.
    @SuppressLint("RestrictedApi")
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val captured = (application as HubApplication).input.key(event)
        if (captured && model.tab.value == 2 && event.keyCode != KeyEvent.KEYCODE_BACK) return true
        return super.dispatchKeyEvent(event)
    }
    override fun dispatchGenericMotionEvent(event: MotionEvent): Boolean {
        val captured = (application as HubApplication).input.motion(event)
        if (captured && model.tab.value == 2) return true
        return super.dispatchGenericMotionEvent(event)
    }
    override fun onStart() {
        super.onStart()
        model.foreground(true)
    }
    override fun onStop() {
        super.onStop()
        (application as HubApplication).input.clearPressed()
        if (!isChangingConfigurations) model.foreground(false)
    }
}
