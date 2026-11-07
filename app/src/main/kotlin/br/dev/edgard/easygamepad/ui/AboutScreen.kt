// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 Edgard
package br.dev.edgard.easygamepad.ui

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import br.dev.edgard.easygamepad.BuildConfig

private enum class AboutDocument(val asset: String) {
    LICENSE("licenses/GPL-3.0.txt"),
    NOTICE("licenses/EasyGamepad-NOTICE.txt"),
    X20CTL("licenses/x20ctl-NOTICE.txt"),
    LEGACY("licenses/x20ctl-MIT-legacy.txt"),
}

@Composable fun AboutScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var document by rememberSaveable { mutableStateOf<AboutDocument?>(null) }
    var linkError by remember { mutableStateOf(false) }
    val openLink: (String) -> Unit = { url ->
        linkError = false
        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri())) }
            .onFailure { linkError = true }
    }
    ScreenColumn {
        TextButton(onClick = onBack, contentPadding = PaddingValues(horizontal = 8.dp)) {
            HubIcon(Glyph.ARROW, Modifier.size(18.dp).rotate(180f), MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(8.dp))
            Text(tr("Back to settings", "Voltar aos ajustes"))
        }
        ScreenHeading(tr("Your controller companion", "Seu parceiro de jogo"), tr("About the app", "Sobre o aplicativo"))
        HubCard(Modifier.fillMaxWidth()) {
            Column(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                BrandMark(Modifier.size(76.dp))
                Text("EasyGamepad", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center)
                Text("Easy + Gamepad", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary)
                Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.primaryContainer) {
                    Text(tr("Version ", "Versão ") + BuildConfig.VERSION_NAME,
                        Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                        style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
                Text(tr("Your controllers, your way. Test buttons and movement, and customize supported controllers.",
                    "Seus controles, do seu jeito. Teste botões e movimentos e personalize os modelos compatíveis."),
                    textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(tr("Created by Edgard", "Criado por Edgard"), style = MaterialTheme.typography.labelLarge)
            }
        }
        HubCard(Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                HubIcon(Glyph.INFO, color = MaterialTheme.colorScheme.primary)
                Text(tr("Free and open source", "Software livre e aberto"), style = MaterialTheme.typography.titleMedium)
            }
            Text("GPL-3.0-or-later", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleLarge)
            Description(tr("Licensed under the GNU GPL, version 3 or later. You can use, share and modify EasyGamepad under this license. Distributed without warranty.",
                "Licenciado sob a GNU GPL, versão 3 ou posterior. Você pode usar, compartilhar e modificar o EasyGamepad sob essa licença. Distribuído sem garantia."))
            TextButton(onClick = { document = AboutDocument.LICENSE }) { Text(tr("Read full license", "Ler licença completa")) }
            TextButton(onClick = { document = AboutDocument.NOTICE }) { Text(tr("Copyright notices", "Avisos de direitos autorais")) }
        }
        SectionTitle(tr("Links", "Links"))
        if (linkError) InfoBanner(tr("Could not open the link. Check that a browser is installed and try again.",
            "Não foi possível abrir o link. Confira se há um navegador instalado e tente novamente."), true)
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            AboutLink(tr("EasyGamepad on GitHub", "EasyGamepad no GitHub"),
                tr("Source code and project updates", "Código-fonte e novidades do projeto"), "github.com/edgardmessias/easygamepad") {
                openLink("https://github.com/edgardmessias/easygamepad")
            }
            AboutLink(tr("GNU GPL license", "Licença GNU GPL"),
                tr("Learn about the software license", "Conheça a licença do aplicativo"), "gnu.org") {
                openLink("https://www.gnu.org/licenses/gpl-3.0.html")
            }
            AboutLink(tr("x20ctl project", "Projeto x20ctl"),
                tr("X10 images and button geometry", "Imagens e posições dos botões do X10"), "github.com/AmjadAAYD/x20ctl") {
                openLink("https://github.com/AmjadAAYD/x20ctl")
            }
            AboutLink(tr("EasySMX X10 support", "Suporte ao EasySMX X10"),
                tr("Manufacturer's manuals and connection guide", "Manuais e orientações de conexão do fabricante"), "easysmx.com") {
                openLink("https://www.easysmx.com/pages/support-about-easysmx-x10-controller")
            }
        }
        Description(tr("Links open in your browser.", "Os links abrem no navegador."))
        HubCard(Modifier.fillMaxWidth()) {
            Text(tr("Credits", "Créditos"), style = MaterialTheme.typography.titleLarge)
            Text("x20ctl · Amjad AAYD", style = MaterialTheme.typography.titleMedium)
            Description(tr("The EasySMX X10 images and button geometry come from x20ctl, under GPL-3.0-or-later. Copyright © 2026 Amjad AAYD.",
                "As imagens e a geometria dos botões do EasySMX X10 vêm do x20ctl, sob GPL-3.0-or-later. Copyright © 2026 Amjad AAYD."))
            TextButton(onClick = { document = AboutDocument.X20CTL }) { Text(tr("Read original notices", "Ler avisos originais")) }
            TextButton(onClick = { document = AboutDocument.LEGACY }) { Text(tr("Legacy MIT notice", "Aviso MIT legado")) }
            Description(tr("EasyGamepad is an independent app, with no affiliation with EasySMX or x20ctl. Product names and trademarks belong to their respective owners.",
                "O EasyGamepad é um aplicativo independente, sem vínculo com a EasySMX ou o x20ctl. Nomes de produtos e marcas pertencem aos respectivos titulares."))
        }
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Description("© 2026 Edgard · GPL-3.0-or-later")
            Text(tr("Android 8.0 or later", "Android 8.0 ou superior"), style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
    document?.let { selected ->
        val title = when (selected) {
            AboutDocument.LICENSE -> tr("GNU GPL license", "Licença GNU GPL")
            AboutDocument.NOTICE -> tr("Copyright notices", "Avisos de direitos autorais")
            AboutDocument.X20CTL -> tr("x20ctl notices", "Avisos do x20ctl")
            AboutDocument.LEGACY -> tr("Legacy MIT notice", "Aviso MIT legado")
        }
        val text = remember(context, selected) { context.assets.open(selected.asset).bufferedReader().use { it.readText() } }
        AlertDialog(onDismissRequest = { document = null }, title = { Text(title) },
            text = { key(selected) { Text(text, Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState()),
                style = MaterialTheme.typography.bodySmall) } },
            confirmButton = { TextButton(onClick = { document = null }) { Text(tr("Close", "Fechar")) } })
    }
}

@Composable private fun AboutLink(title: String, description: String, host: String, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Description(description)
                Text(host, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
            }
            HubIcon(Glyph.ARROW, Modifier.size(19.dp).rotate(-45f), MaterialTheme.colorScheme.primary)
        }
    }
}
