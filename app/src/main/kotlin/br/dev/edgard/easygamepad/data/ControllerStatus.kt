// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 Edgard
package br.dev.edgard.easygamepad.data

import br.dev.edgard.easygamepad.ble.LinkPhase
import br.dev.edgard.easygamepad.ble.LinkState

/** Gameplay and configuration are independent; telemetry must belong to the selected device. */
data class ControllerStatus(
    val gameplayConnected: Boolean,
    val configurationConnected: Boolean,
    val connecting: Boolean,
    val batteryPercent: Int?,
    val charging: Boolean,
) {
    val connected get() = gameplayConnected || configurationConnected
}

fun controllerStatus(device: ControllerDevice?, link: LinkState, hardware: HardwareState): ControllerStatus {
    val selectedLink = device?.address != null && device.address == link.address
    val ready = selectedLink && link.phase == LinkPhase.READY
    val battery = hardware.battery.takeIf { ready }
    return ControllerStatus(
        gameplayConnected = device?.inputId != null,
        configurationConnected = ready,
        connecting = selectedLink && link.phase !in listOf(LinkPhase.READY, LinkPhase.DISCONNECTED),
        batteryPercent = battery?.percent ?: device?.inputBatteryPercent.takeIf { device?.inputId != null },
        charging = battery?.charging ?: (device?.inputBatteryCharging == true && device.inputId != null),
    )
}
