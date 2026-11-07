// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 Edgard
package br.dev.edgard.easygamepad.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.dev.edgard.easygamepad.input.InputState

@Composable fun StatusPill(label: String, active: Boolean, modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary) {
    val ink = if (active) color else MaterialTheme.colorScheme.onSurfaceVariant
    Surface(modifier, shape = CircleShape, color = ink.copy(alpha = .09f), border = BorderStroke(1.dp, ink.copy(alpha = .22f))) {
        Row(Modifier.padding(horizontal = 10.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(Modifier.size(5.dp).background(ink, CircleShape))
            Text(label, color = ink, style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable fun SectionTitle(title: String, action: String? = null, onAction: () -> Unit = {}) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
        if (action != null) TextButton(onClick = onAction, contentPadding = PaddingValues(horizontal = 8.dp)) { Text(action) }
    }
}

/** A quiet photographic stage inspired by x20ctl's controller-first Studio layout. */
@Composable fun ControllerStage(modifier: Modifier = Modifier, x10: Boolean = false, input: InputState? = null, rear: Boolean = false,
    mappings: Map<br.dev.edgard.easygamepad.protocol.PadKey, br.dev.edgard.easygamepad.protocol.PadKey> = emptyMap(),
    onPick: ((br.dev.edgard.easygamepad.protocol.PadKey) -> Unit)? = null) {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val edge = MaterialTheme.colorScheme.outlineVariant
    Box(modifier.clip(RoundedCornerShape(22.dp)), contentAlignment = Alignment.Center) {
        Canvas(Modifier.matchParentSize()) {
            drawRect(Brush.radialGradient(listOf(secondary.copy(alpha = .16f), primary.copy(alpha = .035f), Color.Transparent),
                center = Offset(size.width * .52f, size.height * .5f), radius = size.width * .62f))
            listOf(.38f, .53f).forEach { drawCircle(edge.copy(alpha = .5f), size.width * it, Offset(size.width * .5f, size.height * .58f), style = Stroke(1.dp.toPx())) }
        }
        if (x10) ControllerArt(Modifier.fillMaxSize().padding(horizontal = 4.dp), input, rear, mappings, onPick)
        else GenericControllerArt(Modifier.fillMaxSize().padding(12.dp), input)
    }
}

@Composable fun MetricTile(label: String, value: String, detail: String, glyph: Glyph, modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary, onClick: (() -> Unit)? = null) {
    val body: @Composable () -> Unit = {
        Column(Modifier.padding(13.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                HubIcon(glyph, Modifier.size(16.dp), color)
                Spacer(Modifier.width(6.dp))
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Text(value, fontSize = 27.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-1).sp,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(detail, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2, overflow = TextOverflow.Ellipsis, minLines = 2)
        }
    }
    if (onClick != null) Surface(onClick = onClick, modifier = modifier, shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), content = body)
    else Surface(modifier = modifier, shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), content = body)
}

@Composable fun FeatureTile(title: String, detail: String, glyph: Glyph, color: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = modifier, shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(11.dp), color = color.copy(alpha = .12f)) {
                    HubIcon(glyph, Modifier.padding(9.dp).size(21.dp), color)
                }
                Spacer(Modifier.weight(1f))
                HubIcon(Glyph.ARROW, Modifier.size(16.dp), MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                minLines = 2, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}
