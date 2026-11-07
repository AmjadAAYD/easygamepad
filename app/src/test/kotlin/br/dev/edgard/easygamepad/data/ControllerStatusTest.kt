// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 Edgard
package br.dev.edgard.easygamepad.data

import br.dev.edgard.easygamepad.ble.LinkPhase
import br.dev.edgard.easygamepad.ble.LinkState
import br.dev.edgard.easygamepad.protocol.Battery
import org.junit.Assert.*
import org.junit.Test

class ControllerStatusTest {
    private val device = ControllerDevice("ble:one", "X10", address = "one", inputId = 7)
    private val charge = Battery(62, false, false, 123)

    @Test fun gameplayIsConnectedEvenWhenConfigurationIsOffline() {
        val status = controllerStatus(device, LinkState(), HardwareState())
        assertTrue(status.connected)
        assertTrue(status.gameplayConnected)
        assertFalse(status.configurationConnected)
        assertNull(status.batteryPercent)
    }
    @Test fun androidBatteryRemainsAvailableWithoutConfigurationConnection() {
        val status = controllerStatus(device.copy(inputBatteryPercent = 48, inputBatteryCharging = true), LinkState(), HardwareState())
        assertEquals(48, status.batteryPercent)
        assertTrue(status.charging)
    }
    @Test fun verifiedConfigurationBatteryTakesPriorityForTheSameDevice() {
        val status = controllerStatus(device.copy(inputBatteryPercent = 48), LinkState(LinkPhase.READY, "one"), HardwareState(battery = charge))
        assertEquals(62, status.batteryPercent)
        assertTrue(status.configurationConnected)
        assertFalse(status.charging)
    }
    @Test fun telemetryFromAnotherControllerIsNeverDisplayed() {
        val status = controllerStatus(device, LinkState(LinkPhase.READY, "two"), HardwareState(battery = charge))
        assertFalse(status.configurationConnected)
        assertNull(status.batteryPercent)
    }
    @Test fun sleepClearsConnectionAndDoesNotReuseOldBattery() {
        val status = controllerStatus(device.copy(inputId = null, inputBatteryPercent = 80), LinkState(), HardwareState(battery = charge))
        assertFalse(status.connected)
        assertNull(status.batteryPercent)
    }
    @Test fun reconnectingDoesNotHideTheWorkingGameplayConnection() {
        val status = controllerStatus(device, LinkState(LinkPhase.CONNECTING, "one"), HardwareState())
        assertTrue(status.connecting)
        assertTrue(status.gameplayConnected)
        assertFalse(status.configurationConnected)
        assertFalse(controllerStatus(device, LinkState(LinkPhase.CONNECTING, "two"), HardwareState()).connecting)
    }
    @Test fun realZeroPercentBatteryIsNotTreatedAsMissing() {
        assertEquals(0, controllerStatus(device.copy(inputBatteryPercent = 0), LinkState(), HardwareState()).batteryPercent)
    }
}
