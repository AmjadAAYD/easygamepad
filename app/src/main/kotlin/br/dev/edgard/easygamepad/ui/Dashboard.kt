// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 Edgard
package br.dev.edgard.easygamepad.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.dev.edgard.easygamepad.HubViewModel
import br.dev.edgard.easygamepad.ble.LinkState
import br.dev.edgard.easygamepad.data.HardwareState
import br.dev.edgard.easygamepad.data.controllerStatus

@OptIn(ExperimentalLayoutApi::class)
@Composable fun Dashboard(model: HubViewModel, hardware: HardwareState, link: LinkState) {
    val selected by model.selectedDevice.collectAsStateWithLifecycle()
    val devices by model.devices.collectAsStateWithLifecycle()
    val prefs by model.preferences.collectAsStateWithLifecycle()
    val input by model.input.collectAsStateWithLifecycle()
    val switching by model.switching.collectAsStateWithLifecycle()
    val status = controllerStatus(selected, link, hardware)
    val metricColumns = if (LocalDensity.current.fontScale >= 1.5f) 1 else 3
    var rear by rememberSaveable(selected?.id) { mutableStateOf(false) }
    ScreenColumn {
        val device = selected
        if (device == null) {
            ScreenHeading(tr("Welcome to EasyGamepad", "Bem-vindo ao EasyGamepad"), tr("Ready for your next game.", "Pronto para a próxima partida."))
            ControllerStage(Modifier.fillMaxWidth().height(200.dp), x10 = false)
            HubCard(Modifier.fillMaxWidth()) {
                Text(tr("Your controllers, together.", "Seus controles em um só lugar."), style = MaterialTheme.typography.titleLarge)
                Description(tr("Connect a controller by Bluetooth or USB to test it. Add an X10 to customize its buttons and settings.",
                    "Conecte um controle por Bluetooth ou USB para testar. Adicione um X10 para personalizar botões e ajustes."))
                Button(onClick = { model.deviceSheet.value = true }, modifier = Modifier.fillMaxWidth()) { Text(tr("Add controller", "Adicionar controle")) }
            }
        } else {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Eyebrow(tr("Your controller", "Seu controle"), color = MaterialTheme.colorScheme.primary)
                    Text(device.name, style = MaterialTheme.typography.headlineLarge)
                    Description((if (device.supportsConfiguration && device.name != "EasySMX X10") "EasySMX X10 · " else "") + device.shortId)
                }
                TextButton(onClick = { model.deviceSheet.value = true }) { Text(tr("Manage", "Gerenciar")) }
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusPill(if (status.gameplayConnected) tr("Game connected", "Jogo conectado") else tr("Game offline", "Jogo desconectado"), status.gameplayConnected)
                if (device.supportsConfiguration) StatusPill(when {
                    status.configurationConnected -> tr("App connected", "App conectado")
                    status.connecting -> tr("Connecting…", "Conectando…")
                    else -> tr("App offline", "App desconectado")
                }, status.configurationConnected, color = MaterialTheme.colorScheme.secondary)
            }
            if (devices.size > 1) Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                devices.forEach { row -> FilterChip(row.id == device.id, { if (!switching) model.chooseDevice(row) },
                    label = { Text(row.name) }, enabled = !switching && !hardware.busy && !hardware.calibration.stopRequired) }
            }
            if (device.supportsConfiguration) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                SingleChoiceSegmentedButtonRow {
                    listOf(tr("Front", "Frente"), tr("Back", "Traseira")).forEachIndexed { index, title ->
                        SegmentedButton(selected = rear == (index == 1), onClick = { rear = index == 1 },
                            shape = SegmentedButtonDefaults.itemShape(index, 2), label = { Text(title) })
                    }
                }
            }
            ControllerStage(Modifier.fillMaxWidth().height(188.dp), device.usesX10Art,
                input.takeIf { status.gameplayConnected }, rear)
            FlowRow(maxItemsInEachRow = metricColumns, horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricTile(tr("Battery", "Bateria"), status.batteryPercent?.let { "$it%" } ?: "—",
                    when {
                        status.charging -> tr("Charging", "Carregando")
                        status.batteryPercent != null -> tr("Tap to refresh", "Toque para atualizar")
                        status.configurationConnected -> tr("Reading charge…", "Consultando carga…")
                        device.supportsConfiguration -> tr("Connect to app", "Conecte ao app")
                        else -> tr("Not reported", "Não informada")
                    }, Glyph.BATTERY, Modifier.weight(1f), onClick = model::refreshBattery)
                MetricTile(tr("Status", "Estado"), if (status.connected) tr("Online", "Online") else tr("Offline", "Offline"),
                    if (status.gameplayConnected) tr("Ready to play", "Pronto para jogar") else if (status.configurationConnected) tr("App connected", "App conectado") else tr("Turn on controller", "Ligue o controle"),
                    Glyph.BLUETOOTH, Modifier.weight(1f), color = MaterialTheme.colorScheme.tertiary)
                if (device.supportsConfiguration) MetricTile(tr("Profiles", "Perfis"), prefs.profiles.size.toString().padStart(2, '0'),
                    tr("Saved on phone", "Salvos no celular"), Glyph.PROFILES, Modifier.weight(1f), color = MaterialTheme.colorScheme.secondary,
                    onClick = { model.customize() })
            }
            if (device.supportsConfiguration && !status.configurationConnected) HubCard(Modifier.fillMaxWidth()) {
                Text(tr("Battery & customization", "Bateria e personalização"), style = MaterialTheme.typography.titleMedium)
                Description(if (status.gameplayConnected) tr("Your game is connected. Connect to QMacro for battery and settings. Disconnect it on other phones first.",
                    "O jogo já está conectado. Conecte ao QMacro para consultar a carga e os ajustes. Desconecte de outros celulares antes.")
                    else tr("Turn on your controller in BT mode and keep it nearby.", "Ligue o controle no modo BT e mantenha por perto."))
                if (status.connecting) LinearProgressIndicator(Modifier.fillMaxWidth())
                Button(onClick = model::connectSelected, enabled = !status.connecting && !switching, modifier = Modifier.fillMaxWidth()) {
                    HubIcon(Glyph.BLUETOOTH, Modifier.size(18.dp), MaterialTheme.colorScheme.onPrimary)
                    Spacer(Modifier.width(8.dp)); Text(if (status.connecting) tr("Connecting…", "Conectando…") else tr("Connect to app", "Conectar ao app"))
                }
                TextButton(onClick = { model.connectionSheet.value = true }) { Text(tr("Find configuration connection", "Buscar conexão do controle")) }
            }
            SectionTitle(tr("Make it yours", "Do seu jeito"))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FeatureTile(tr("Test", "Testar"), tr("See every button and movement", "Confira cada botão e movimento"), Glyph.TEST,
                    MaterialTheme.colorScheme.secondary, Modifier.weight(1f)) { model.tab.value = 2 }
                if (device.supportsConfiguration) FeatureTile(tr("Buttons", "Botões"), tr("Choose what each button does", "Escolha a função de cada botão"), Glyph.EDIT,
                    MaterialTheme.colorScheme.primary, Modifier.weight(1f)) { model.customize(0) }
            }
            if (device.supportsConfiguration) Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FeatureTile(tr("Sticks", "Analógicos"), tr("Tune sensitivity and movement", "Ajuste a resposta dos movimentos"), Glyph.TOOLS,
                    MaterialTheme.colorScheme.tertiary, Modifier.weight(1f)) { model.customize(1) }
                FeatureTile(tr("Vibration", "Vibração"), tr("Find your ideal intensity", "Encontre a intensidade ideal"), Glyph.SETTINGS,
                    MaterialTheme.colorScheme.secondary, Modifier.weight(1f)) { model.customize(5) }
            }
            if (status.configurationConnected) TextButton(onClick = model::disconnect) { Text(tr("Disconnect from app", "Desconectar do app")) }
        }
    }
}
