// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 Edgard
package br.dev.edgard.easygamepad.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import android.view.KeyEvent
import br.dev.edgard.easygamepad.input.InputState

enum class Glyph { HUB, PROFILES, TEST, TOOLS, SETTINGS, BLUETOOTH, BATTERY, ARROW, PLUS, CHECK, CLOSE, DOWNLOAD, RESET, LANGUAGE, INFO, SHARE, EDIT, TRASH }

@Composable fun HubIcon(glyph: Glyph, modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.onSurface, description: String? = null) {
    val semantics = if (description != null) Modifier.semantics { contentDescription = description } else Modifier
    Canvas(modifier.then(semantics).size(24.dp)) {
        val factor = size.minDimension / 24f
        scale(factor, factor, Offset.Zero) {
            fun line(x: Float, y: Float, x2: Float, y2: Float) = drawLine(color, Offset(x, y), Offset(x2, y2), 1.65f, StrokeCap.Round)
            fun rect(x: Float, y: Float, w: Float, h: Float) = drawRoundRect(color, Offset(x, y), Size(w, h), androidx.compose.ui.geometry.CornerRadius(2f), style = Stroke(1.65f))
            when (glyph) {
                Glyph.HUB -> { rect(3f, 3f, 7f, 7f); rect(14f, 3f, 7f, 7f); rect(3f, 14f, 7f, 7f); rect(14f, 14f, 7f, 7f) }
                Glyph.PROFILES -> { rect(4f, 7f, 16f, 14f); line(7f, 3f, 17f, 3f); line(8f, 12f, 16f, 12f); line(8f, 16f, 13f, 16f) }
                Glyph.TEST -> { rect(2f, 6f, 20f, 13f); line(6f, 12f, 12f, 12f); line(9f, 9f, 9f, 15f); drawCircle(color, 1.3f, Offset(17f, 11f)); drawCircle(color, 1.3f, Offset(19f, 14f)) }
                Glyph.TOOLS -> { drawCircle(color, 8f, Offset(12f, 12f), style = Stroke(1.65f)); drawCircle(color, 3f, Offset(12f, 12f), style = Stroke(1.65f)); line(12f, 1f, 12f, 6f); line(12f, 18f, 12f, 23f); line(1f, 12f, 6f, 12f); line(18f, 12f, 23f, 12f) }
                Glyph.SETTINGS -> { for (y in listOf(6f, 12f, 18f)) line(3f, y, 21f, y); rect(7f, 3f, 3f, 6f); rect(15f, 9f, 3f, 6f); rect(8f, 15f, 3f, 6f) }
                Glyph.BLUETOOTH -> { line(12f, 2f, 12f, 22f); line(12f, 2f, 18f, 7f); line(18f, 7f, 6f, 17f); line(6f, 7f, 18f, 17f); line(18f, 17f, 12f, 22f) }
                Glyph.BATTERY -> { rect(2f, 6f, 18f, 12f); line(23f, 10f, 23f, 14f); line(6f, 10f, 6f, 14f); line(10f, 10f, 10f, 14f); line(14f, 10f, 14f, 14f) }
                Glyph.ARROW -> { line(5f, 12f, 19f, 12f); line(14f, 7f, 19f, 12f); line(14f, 17f, 19f, 12f) }
                Glyph.PLUS -> { line(5f, 12f, 19f, 12f); line(12f, 5f, 12f, 19f) }
                Glyph.CHECK -> { line(4f, 12f, 9f, 17f); line(9f, 17f, 20f, 6f) }
                Glyph.CLOSE -> { line(6f, 6f, 18f, 18f); line(6f, 18f, 18f, 6f) }
                Glyph.DOWNLOAD -> { line(12f, 3f, 12f, 16f); line(7f, 11f, 12f, 16f); line(17f, 11f, 12f, 16f); line(4f, 18f, 4f, 21f); line(4f, 21f, 20f, 21f); line(20f, 21f, 20f, 18f) }
                Glyph.RESET -> { drawArc(color, 200f, 300f, false, Offset(4f, 4f), Size(16f, 16f), style = Stroke(1.65f)); line(2f, 3f, 3f, 10f); line(3f, 10f, 10f, 9f) }
                Glyph.LANGUAGE -> { drawCircle(color, 9f, Offset(12f, 12f), style = Stroke(1.65f)); line(3f, 12f, 21f, 12f); drawOval(color, Offset(8f, 3f), Size(8f, 18f), style = Stroke(1.65f)) }
                Glyph.INFO -> { drawCircle(color, 9f, Offset(12f, 12f), style = Stroke(1.65f)); line(12f, 11f, 12f, 17f); drawCircle(color, 1f, Offset(12f, 7f)) }
                Glyph.SHARE -> { line(5f, 12f, 19f, 5f); line(5f, 12f, 19f, 19f); for (p in listOf(Offset(5f, 12f), Offset(19f, 5f), Offset(19f, 19f))) drawCircle(color, 2.5f, p, style = Stroke(1.65f)) }
                Glyph.EDIT -> { line(4f, 20f, 8f, 19f); line(8f, 19f, 21f, 6f); line(21f, 6f, 17f, 2f); line(17f, 2f, 4f, 15f); line(4f, 15f, 4f, 20f); line(14f, 5f, 18f, 9f) }
                Glyph.TRASH -> { rect(6f, 7f, 12f, 14f); line(3f, 5f, 21f, 5f); line(9f, 2f, 15f, 2f); line(10f, 10f, 10f, 17f); line(14f, 10f, 14f, 17f) }
            }
        }
    }
}

@Composable fun BrandMark(modifier: Modifier = Modifier) {
    androidx.compose.foundation.Image(
        painter = androidx.compose.ui.res.painterResource(br.dev.edgard.easygamepad.R.drawable.ic_brand),
        contentDescription = "EasyGamepad", modifier = modifier,
    )
}

@Composable fun GenericControllerArt(modifier: Modifier = Modifier, input: InputState? = null) {
    val accent = MaterialTheme.colorScheme.primary
    val outline = MaterialTheme.colorScheme.outline
    val description = tr("Generic gamepad. Buttons and sticks respond to the selected controller.", "Controle genérico. Botões e analógicos acompanham o controle selecionado.")
    Canvas(modifier.semantics { contentDescription = description }) {
        val factor = minOf(size.width / 320f, size.height / 190f)
        translate((size.width - 320f * factor) / 2, (size.height - 190f * factor) / 2) {
            scale(factor, factor, Offset.Zero) {
                val body = Path().apply {
                    moveTo(91f, 36f); cubicTo(60f, 30f, 48f, 46f, 38f, 80f)
                    lineTo(22f, 140f); cubicTo(16f, 169f, 41f, 181f, 60f, 161f)
                    lineTo(91f, 128f); lineTo(229f, 128f); lineTo(260f, 161f)
                    cubicTo(279f, 181f, 304f, 169f, 298f, 140f); lineTo(282f, 80f)
                    cubicTo(272f, 46f, 260f, 30f, 229f, 36f); close()
                }
                drawPath(body, Brush.verticalGradient(listOf(Color(0xFF3C475B), Color(0xFF171F2D)), 35f, 170f))
                drawPath(body, outline, style = Stroke(1.5f))
                drawLine(accent, Offset(73f, 62f), Offset(113f, 62f), 3f, StrokeCap.Round)
                drawLine(accent, Offset(93f, 42f), Offset(93f, 82f), 3f, StrokeCap.Round)
                listOf(Triple(233f, 49f, KeyEvent.KEYCODE_BUTTON_Y), Triple(215f, 67f, KeyEvent.KEYCODE_BUTTON_X),
                    Triple(251f, 67f, KeyEvent.KEYCODE_BUTTON_B), Triple(233f, 85f, KeyEvent.KEYCODE_BUTTON_A)).forEach { (x,y,key) ->
                    drawCircle(if (key in (input?.pressed ?: emptySet())) accent else Color(0xFF8D9AAA), 7f, Offset(x,y))
                }
                for ((x, ax, ay) in listOf(Triple(123f, 0, 1), Triple(197f, 11, 14))) {
                    drawCircle(Color(0xFF101722), 20f, Offset(x, 110f))
                    drawCircle(outline, 20f, Offset(x, 110f), style = Stroke(1.5f))
                    drawCircle(Color(0xFF53617B), 13f, Offset(x + (input?.axes?.get(ax) ?: 0f) * 7, 110f + (input?.axes?.get(ay) ?: 0f) * 7))
                }
            }
        }
    }
}
