// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 Edgard
package br.dev.edgard.easygamepad.data

import br.dev.edgard.easygamepad.protocol.ControllerConfig

/** Local editing workspaces follow identity, never display name or connection timing. */
class ControllerDrafts {
    private val saved = mutableMapOf<String, ControllerConfig>()

    fun switch(from: String?, to: String?, current: ControllerConfig, transferSetup: Boolean = false): ControllerConfig {
        from?.let { saved[it] = current }
        if (transferSetup && to != null) saved[to] = current
        return to?.let(saved::get) ?: ControllerConfig()
    }
}
