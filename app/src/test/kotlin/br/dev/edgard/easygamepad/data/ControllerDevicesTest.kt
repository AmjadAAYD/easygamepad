// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 Edgard
package br.dev.edgard.easygamepad.data

import br.dev.edgard.easygamepad.input.InputPad
import org.junit.Assert.*
import org.junit.Test

class ControllerDevicesTest {
    private fun pad(id: Int, descriptor: String) = InputPad(id, "Xbox Wireless Controller", 1118, 736, false, descriptor)

    @Test fun genericOrUnknownModelNeverGainsConfigurationFromItsDisplayName() {
        listOf(null, "unknown-brand", "ipega").forEach { id ->
            val device = ControllerDevice("hid:7", "EasySMX X10", modelId = id, inputId = 7)
            assertFalse(device.supportsConfiguration)
            assertFalse(device.usesX10Art)
            assertTrue(device.rearButtons.isEmpty())
            assertEquals(setOf(br.dev.edgard.easygamepad.protocol.DeviceCapability.INPUT_TEST), device.capabilities)
        }
        val x10 = ControllerDevice("saved", "Sala", modelId = br.dev.edgard.easygamepad.protocol.DeviceProfile.X10.id)
        assertTrue(x10.supportsConfiguration)
        assertTrue(x10.usesX10Art)
        assertEquals(listOf(br.dev.edgard.easygamepad.protocol.PadKey.M1, br.dev.edgard.easygamepad.protocol.PadKey.M2), x10.rearButtons)
    }

    @Test fun singleX10AndSingleGamepadWorkWithoutManualAssociation() {
        val saved = listOf(SavedController("AA:BB:CC:DD:EE:01"))
        val devices = controllerDevices(saved, listOf(pad(3, "xbox")))
        assertEquals(1, devices.size)
        assertEquals(3, devices.single().inputId)
        assertTrue(devices.single().automaticInput)
        assertNull(devices.single().inputDescriptor) // Temporary input choice does not invent a saved hardware identity.
        assertNull(controllerDevices(saved, listOf(pad(3, "xbox")), mapOf(saved[0].address to -1))[0].inputId)
    }
    @Test fun multipleUnlinkedGamepadsRequireUserChoice() {
        val devices = controllerDevices(listOf(SavedController("AA:BB:CC:DD:EE:01")), listOf(pad(3, "one"), pad(7, "two")))
        assertEquals(3, devices.size)
        assertNull(devices[0].inputId)
        assertFalse(devices[0].automaticInput)
    }

    @Test fun identicalNamesRemainSeparateAndAreNotAssumedToBeX10() {
        val devices = controllerDevices(emptyList(), listOf(pad(3, "one"), pad(7, "two")))
        assertEquals(2, devices.size)
        assertEquals(setOf(3, 7), devices.map { it.inputId }.toSet())
        assertEquals(2, devices.map { it.id }.distinct().size)
        assertTrue(devices.none { it.isX10 })
    }
    @Test fun explicitHidAssociationJoinsOnlyOneConfigurationDevice() {
        val saved = listOf(SavedController("AA:BB:CC:DD:EE:01", inputDescriptor = "one"), SavedController("AA:BB:CC:DD:EE:02", inputDescriptor = "two"))
        val devices = controllerDevices(saved, listOf(pad(3, "one"), pad(7, "two")))
        assertEquals(2, devices.size)
        assertEquals(3, devices[0].inputId)
        assertEquals(7, devices[1].inputId)
        assertTrue(devices.all { it.isX10 })
    }
    @Test fun descriptorCollisionRequiresRuntimeChoiceAndDoesNotHideOtherPad() {
        val saved = listOf(SavedController("AA:BB:CC:DD:EE:01", inputDescriptor = "same"))
        val pads = listOf(pad(3, "same"), pad(7, "same"))
        val unlinked = controllerDevices(saved, pads)
        assertEquals(3, unlinked.size)
        assertNull(unlinked[0].inputId)
        assertEquals(3, unlinked.map { it.id }.distinct().size)
        val chosen = controllerDevices(saved, pads, mapOf(saved[0].address to 7))
        assertEquals(2, chosen.size)
        assertEquals(7, chosen[0].inputId)
        assertEquals(3, chosen[1].inputId)
    }
    @Test fun disconnectingChosenPadNeverSilentlySelectsItsIdenticalNeighbour() {
        val saved = listOf(SavedController("AA:BB:CC:DD:EE:01", inputDescriptor = "same"))
        val devices = controllerDevices(saved, listOf(pad(3, "same")), mapOf(saved[0].address to 7))
        assertNull(devices[0].inputId)
        assertEquals(3, devices[1].inputId)
    }

    @Test fun renamingOneOfTwoIdenticalModelsPreservesIdentityAndConnectionSettings() {
        val saved = listOf(SavedController("AA:BB:CC:DD:EE:01", inputDescriptor = "one"),
            SavedController("AA:BB:CC:DD:EE:02", inputDescriptor = "two"))
        val renamed = renameController(saved, saved[1].address, "  Jogador 2 🎮  ")
        assertEquals(saved[0], renamed[0])
        assertEquals(saved[1].copy(name = "Jogador 2 🎮"), renamed[1])
        val devices = controllerDevices(renamed, listOf(pad(3, "one"), pad(7, "two")))
        assertEquals(listOf("EasySMX X10", "Jogador 2 🎮"), devices.map { it.name })
        assertEquals(listOf(3, 7), devices.map { it.inputId })
        assertEquals(renamed[1].name, controllerDevices(renamed, emptyList())[1].name)
    }

    @Test fun savedInputNamesFollowDescriptorsAcrossRuntimeIdChanges() {
        val names = mapOf("one" to "Sala", "two" to "Jogador 2")
        val devices = controllerDevices(emptyList(), listOf(pad(45, "two"), pad(9, "one")), inputNames = names)
        assertEquals(listOf("Jogador 2", "Sala"), devices.map { it.name })
    }

    @Test fun androidChargeStaysWithTheLinkedPhysicalController() {
        val saved = listOf(SavedController("one", inputDescriptor = "a"), SavedController("two", inputDescriptor = "b"))
        val devices = controllerDevices(saved, listOf(pad(4, "b").copy(batteryPercent = 90), pad(7, "a").copy(batteryPercent = 25)))
        assertEquals(listOf(25, 90), devices.map { it.inputBatteryPercent })
        assertEquals(listOf(7, 4), devices.map { it.inputId })
    }

    @Test fun ambiguousDescriptorsNeverShareAPersistedNickname() {
        val pads = listOf(pad(3, "same"), pad(7, "same"))
        val devices = controllerDevices(emptyList(), pads, inputNames = mapOf("same" to "Sala"), sessionNames = mapOf("7:same" to "Jogador 2"))
        assertEquals(listOf("Xbox Wireless Controller", "Jogador 2"), devices.map { it.name })
    }

    @Test fun invalidNamesAreRejectedWithoutChangingSavedControllers() {
        val saved = listOf(SavedController("AA:BB:CC:DD:EE:01"))
        listOf("  ", "x".repeat(41), "Sala\n2").forEach { name ->
            assertTrue(runCatching { renameController(saved, saved[0].address, name) }.isFailure)
        }
        assertTrue(runCatching { renameController(saved, "missing", "Sala") }.isFailure)
        assertEquals("EasySMX X10", saved.single().name)
    }
}
