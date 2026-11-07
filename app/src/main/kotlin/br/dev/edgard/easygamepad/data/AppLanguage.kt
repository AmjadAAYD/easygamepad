// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 Edgard
package br.dev.edgard.easygamepad.data

/** Device locales are tried in order. Unknown or unavailable locales fall back to English. */
object AppLanguage {
    fun resolve(preference: String, systemLanguages: List<String>): String = when (preference) {
        "en", "pt-BR" -> preference
        else -> systemLanguages.firstNotNullOfOrNull { tag -> when (tag.substringBefore('-').lowercase()) {
            "pt" -> "pt-BR"
            "en" -> "en"
            else -> null
        } } ?: "en"
    }
}
