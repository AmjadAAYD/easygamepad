// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 Edgard
package br.dev.edgard.easygamepad.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import br.dev.edgard.easygamepad.data.AppLanguage
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val Lime = Color(0xFFC6F568)
val Lavender = Color(0xFFB9A5FF)
val Aqua = Color(0xFF7AD7D0)
val LocalPortuguese = staticCompositionLocalOf { false }
@Composable fun tr(en: String, pt: String) = if (LocalPortuguese.current) pt else en

@Composable fun resolvedLanguage(preference: String): String {
    // Observe configuration updates, but read the device locales, not a previous app override.
    val configuration = LocalConfiguration.current
    return remember(preference, configuration) {
        val locales = android.content.res.Resources.getSystem().configuration.locales
        AppLanguage.resolve(preference, (0 until locales.size()).map { locales[it].toLanguageTag() })
    }
}

@Composable fun HubTheme(theme: String, language: String, content: @Composable () -> Unit) {
    val dark = theme == "dark" || (theme == "system" && isSystemInDarkTheme())
    val colors = if (dark) darkColorScheme(
        primary = Lime, onPrimary = Color(0xFF1B260D), primaryContainer = Color(0xFF26321A), onPrimaryContainer = Lime,
        secondary = Lavender, secondaryContainer = Color(0xFF302840), onSecondaryContainer = Color(0xFFE6DBFF),
        tertiary = Aqua, background = Color(0xFF0D1017), onBackground = Color(0xFFF1F2F5),
        surface = Color(0xFF151922), onSurface = Color(0xFFF1F2F5), surfaceVariant = Color(0xFF202632),
        onSurfaceVariant = Color(0xFF9CA5B6), outline = Color(0xFF343C49), outlineVariant = Color(0xFF272E3B),
        error = Color(0xFFFFB4AB), errorContainer = Color(0xFF472322), onErrorContainer = Color(0xFFFFDAD6),
    ) else lightColorScheme(
        primary = Color(0xFF456B16), onPrimary = Color.White, primaryContainer = Color(0xFFE1F3C4), onPrimaryContainer = Color(0xFF263D0A),
        secondary = Color(0xFF6A4EB0), secondaryContainer = Color(0xFFEDE4FF), onSecondaryContainer = Color(0xFF3E2870),
        tertiary = Color(0xFF19736C), background = Color(0xFFF5F6F2), onBackground = Color(0xFF171C23),
        surface = Color.White, onSurface = Color(0xFF171C23), surfaceVariant = Color(0xFFECF0E8),
        onSurfaceVariant = Color(0xFF647062), outline = Color(0xFF818B7D), outlineVariant = Color(0xFFDEE4D8),
    )
    CompositionLocalProvider(LocalPortuguese provides (resolvedLanguage(language) == "pt-BR")) {
        MaterialTheme(colorScheme = colors, typography = Typography(
            displayLarge = androidx.compose.ui.text.TextStyle(fontSize = 56.sp, fontWeight = FontWeight.Bold, letterSpacing = (-2).sp),
            headlineLarge = androidx.compose.ui.text.TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold, letterSpacing = (-.8).sp),
            headlineMedium = androidx.compose.ui.text.TextStyle(fontSize = 23.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.5).sp),
            titleLarge = androidx.compose.ui.text.TextStyle(fontSize = 20.sp, fontWeight = FontWeight.SemiBold),
            titleMedium = androidx.compose.ui.text.TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
            bodyLarge = androidx.compose.ui.text.TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
            bodyMedium = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, lineHeight = 20.sp),
            labelLarge = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
            labelSmall = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, letterSpacing = 1.sp, fontWeight = FontWeight.Medium),
        ), shapes = Shapes(medium = RoundedCornerShape(18.dp), large = RoundedCornerShape(24.dp), extraLarge = RoundedCornerShape(30.dp))) {
            content()
        }
    }
}

@Composable fun Eyebrow(text: String, modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.onSurfaceVariant) {
    Text(text.uppercase(), modifier, color = color, style = MaterialTheme.typography.labelSmall)
}
@Composable fun Description(text: String, modifier: Modifier = Modifier) {
    Text(text, modifier, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
}
@Composable fun Mono(text: String, modifier: Modifier = Modifier) {
    Text(text, modifier, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
}
@Composable fun HubCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Surface(modifier, shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = .65f))) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp), content = content)
    }
}
