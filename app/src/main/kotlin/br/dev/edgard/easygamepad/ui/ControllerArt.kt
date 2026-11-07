// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 Edgard
package br.dev.edgard.easygamepad.ui

import android.view.KeyEvent
import android.view.MotionEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.material3.MaterialTheme
import androidx.core.graphics.PathParser
import br.dev.edgard.easygamepad.R
import br.dev.edgard.easygamepad.input.InputState
import br.dev.edgard.easygamepad.protocol.PadKey
import org.json.JSONObject

private data class ArtRegion(val key: PadKey, val bounds: Rect, val path: Path)
private data class StickLayer(val center: Offset, val travel: Float, val radius: Float)
private data class X10ArtLayout(val front: List<ArtRegion>, val rear: List<ArtRegion>, val sticks: List<StickLayer>)

/** Asset geometry from x20ctl; see THIRD_PARTY.md. Android input and rendering are independent. */
private fun readLayout(json: String): X10ArtLayout {
    val root = JSONObject(json)
    val shapes = root.getJSONObject("shapes")
    fun regions(group: String): List<ArtRegion> {
        val rows = shapes.getJSONObject(group)
        return rows.keys().asSequence().map { name ->
            val key = when (name) {
                "DPAD_UP" -> PadKey.UP; "DPAD_DOWN" -> PadKey.DOWN
                "DPAD_LEFT" -> PadKey.LEFT; "DPAD_RIGHT" -> PadKey.RIGHT
                else -> PadKey.valueOf(name)
            }
            val row = rows.getJSONObject(name)
            val b = row.getJSONArray("bounds")
            val x = b.getDouble(0).toFloat(); val y = b.getDouble(1).toFloat()
            ArtRegion(key, Rect(x, y, x + b.getDouble(2).toFloat(), y + b.getDouble(3).toFloat()),
                requireNotNull(PathParser.createPathFromPathData(row.getString("path"))).asComposePath())
        }.toList()
    }
    val sticks = listOf("left", "right").map { side ->
        val row = root.getJSONObject("layers").getJSONObject("sticks").getJSONObject(side)
        val c = row.getJSONArray("center")
        StickLayer(Offset(c.getDouble(0).toFloat(), c.getDouble(1).toFloat()),
            row.getDouble("travel").toFloat(), row.getDouble("capRadius").toFloat())
    }
    val clicks = sticks.mapIndexed { index, stick ->
        val r = stick.radius
        ArtRegion(if (index == 0) PadKey.LS else PadKey.RS,
            Rect(stick.center - Offset(r, r), stick.center + Offset(r, r)),
            Path().apply { addOval(Rect(stick.center - Offset(r, r), stick.center + Offset(r, r))) })
    }
    return X10ArtLayout(regions("front") + clicks, regions("back") + regions("macros"), sticks)
}

private val androidKeys = mapOf(
    PadKey.A to KeyEvent.KEYCODE_BUTTON_A, PadKey.B to KeyEvent.KEYCODE_BUTTON_B,
    PadKey.X to KeyEvent.KEYCODE_BUTTON_X, PadKey.Y to KeyEvent.KEYCODE_BUTTON_Y,
    PadKey.LB to KeyEvent.KEYCODE_BUTTON_L1, PadKey.RB to KeyEvent.KEYCODE_BUTTON_R1,
    PadKey.LT to KeyEvent.KEYCODE_BUTTON_L2, PadKey.RT to KeyEvent.KEYCODE_BUTTON_R2,
    PadKey.LS to KeyEvent.KEYCODE_BUTTON_THUMBL, PadKey.RS to KeyEvent.KEYCODE_BUTTON_THUMBR,
    PadKey.SELECT to KeyEvent.KEYCODE_BUTTON_SELECT, PadKey.START to KeyEvent.KEYCODE_BUTTON_START,
    PadKey.UP to KeyEvent.KEYCODE_DPAD_UP, PadKey.DOWN to KeyEvent.KEYCODE_DPAD_DOWN,
    PadKey.LEFT to KeyEvent.KEYCODE_DPAD_LEFT, PadKey.RIGHT to KeyEvent.KEYCODE_DPAD_RIGHT,
)

@Composable fun ControllerArt(modifier: Modifier = Modifier, input: InputState? = null, rear: Boolean = false,
    mappings: Map<PadKey, PadKey> = emptyMap(), onPick: ((PadKey) -> Unit)? = null) {
    val context = LocalContext.current
    val layout = remember(context) { context.assets.open("controllers/x10-layout.json").bufferedReader().use { readLayout(it.readText()) } }
    val body = ImageBitmap.imageResource(if (rear) R.drawable.x10_rear else R.drawable.x10_front_body)
    val leftCap = ImageBitmap.imageResource(R.drawable.x10_stick_left)
    val rightCap = ImageBitmap.imageResource(R.drawable.x10_stick_right)
    val accent = MaterialTheme.colorScheme.primary
    val ink = Color(0xFF101926)
    val regions = if (rear) layout.rear else layout.front
    val pick by rememberUpdatedState(onPick)
    val description = if (rear) tr("EasySMX X10, back. Two rear buttons: M1 and M2.", "EasySMX X10, traseira. Dois botões traseiros: M1 e M2.")
        else tr("EasySMX X10, front.", "EasySMX X10, frente.")
    val changeLabel = tr("Change button ", "Alterar botão ")
    val touches = if (onPick == null) Modifier else Modifier.pointerInput(rear, layout) {
        detectTapGestures { tap ->
            val factor = minOf(size.width / 1536f, size.height / 1024f)
            val origin = Offset((size.width - 1536 * factor) / 2, (size.height - 1024 * factor) / 2)
            val point = (tap - origin) / factor
            regions.filter { it.bounds.inflate(16f).contains(point) }
                .minByOrNull { (it.bounds.center - point).getDistanceSquared() }?.let { pick?.invoke(it.key) }
        }
    }
    Canvas(modifier.then(touches).semantics {
        contentDescription = description
        if (onPick != null) customActions = regions.map { region ->
            CustomAccessibilityAction(changeLabel + region.key.label) { onPick(region.key); true }
        }
    }) {
        val factor = minOf(size.width / 1536f, size.height / 1024f)
        translate((size.width - 1536 * factor) / 2, (size.height - 1024 * factor) / 2) {
            scale(factor, factor, Offset.Zero) {
                drawImage(body)
                if (!rear) listOf(leftCap, rightCap).forEachIndexed { index, cap ->
                    val x = if (index == 0) input?.axes?.get(MotionEvent.AXIS_X) ?: 0f
                        else input?.axes?.get(MotionEvent.AXIS_Z) ?: input?.axes?.get(MotionEvent.AXIS_RX) ?: 0f
                    val y = if (index == 0) input?.axes?.get(MotionEvent.AXIS_Y) ?: 0f
                        else input?.axes?.get(MotionEvent.AXIS_RZ) ?: input?.axes?.get(MotionEvent.AXIS_RY) ?: 0f
                    val travel = layout.sticks[index].travel
                    translate(x.coerceIn(-1f, 1f) * travel, y.coerceIn(-1f, 1f) * travel) { drawImage(cap) }
                }
                if (!rear && input?.pressed?.contains(KeyEvent.KEYCODE_BUTTON_MODE) == true) {
                    drawCircle(accent.copy(alpha = .65f), 53f, Offset(775f, 235f))
                    drawCircle(accent, 53f, Offset(775f, 235f), style = Stroke(7f))
                }
                regions.forEach { region ->
                    val key = region.key
                    val pressed = input != null && (androidKeys[key] in input.pressed || when (key) {
                        PadKey.LEFT -> (input.axes[MotionEvent.AXIS_HAT_X] ?: 0f) < -.5f
                        PadKey.RIGHT -> (input.axes[MotionEvent.AXIS_HAT_X] ?: 0f) > .5f
                        PadKey.UP -> (input.axes[MotionEvent.AXIS_HAT_Y] ?: 0f) < -.5f
                        PadKey.DOWN -> (input.axes[MotionEvent.AXIS_HAT_Y] ?: 0f) > .5f
                        PadKey.LT -> triggerAxis(input, MotionEvent.AXIS_LTRIGGER, MotionEvent.AXIS_BRAKE, KeyEvent.KEYCODE_BUTTON_L2) > .01f
                        PadKey.RT -> triggerAxis(input, MotionEvent.AXIS_RTRIGGER, MotionEvent.AXIS_GAS, KeyEvent.KEYCODE_BUTTON_R2) > .01f
                        PadKey.M1, PadKey.M2 -> input.paddleBindings[key]?.let { it in input.pressed } == true
                        else -> false
                    })
                    val learning = input?.learningPaddle == key
                    val changed = mappings[key]?.let { it != key } == true
                    if (pressed || learning || changed || onPick != null) {
                        drawPath(region.path, accent.copy(alpha = if (pressed) .65f else if (changed) .3f else .09f))
                        drawPath(region.path, if (pressed || learning || changed) accent else Color.White.copy(alpha = .6f), style = Stroke(if (pressed) 7f else 3f))
                    }
                    if (rear && key in listOf(PadKey.M1, PadKey.M2) || changed) {
                        val label = if (changed) "${key.label} → ${mappings[key]?.label}" else key.label
                        val center = region.bounds.center
                        val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                            color = Color.White.toArgb(); textSize = 26f; textAlign = android.graphics.Paint.Align.CENTER
                            typeface = android.graphics.Typeface.DEFAULT_BOLD
                        }
                        val width = paint.measureText(label) + 30f
                        drawRoundRect(ink.copy(alpha = .92f), Offset(center.x - width / 2, center.y - 22f),
                            androidx.compose.ui.geometry.Size(width, 44f), androidx.compose.ui.geometry.CornerRadius(16f))
                        drawContext.canvas.nativeCanvas.drawText(label, center.x, center.y + 9f, paint)
                    }
                }
            }
        }
    }
}
