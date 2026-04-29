package com.shellterminal.presentation.home

import com.shellterminal.domain.model.SSHHost

data class HomeUiState(
    val hosts: List<SSHHost> = emptyList(),
    val selectedHost: SSHHost? = null,
    val isConnected: Boolean = false,
    val connectionState: String = "未连接",
    val terminalOutput: String = "",
    val inputText: String = "",
    val showHostEditor: Boolean = false,
    val editingHost: SSHHost? = null
)