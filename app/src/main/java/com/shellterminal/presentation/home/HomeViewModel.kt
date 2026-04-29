package com.shellterminal.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shellterminal.domain.model.SSHHost
import com.shellterminal.domain.usecase.ConnectionState
import com.shellterminal.domain.usecase.DeleteHostUseCase
import com.shellterminal.domain.usecase.GetAllHostsUseCase
import com.shellterminal.domain.usecase.SaveHostUseCase
import com.shellterminal.domain.usecase.SSHConnectionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getAllHostsUseCase: GetAllHostsUseCase,
    private val saveHostUseCase: SaveHostUseCase,
    private val deleteHostUseCase: DeleteHostUseCase,
    private val connectionManager: SSHConnectionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getAllHostsUseCase().collect { hosts ->
                _uiState.update { it.copy(hosts = hosts) }
            }
        }

        viewModelScope.launch {
            connectionManager.connectionState.collect { state ->
                _uiState.update { currentState ->
                    currentState.copy(
                        isConnected = state is ConnectionState.Connected,
                        connectionState = when (state) {
                            is ConnectionState.Disconnected -> "未连接"
                            is ConnectionState.Connecting -> "连接中..."
                            is ConnectionState.Connected -> "已连接"
                            is ConnectionState.Error -> "错误: ${state.message}"
                        }
                    )
                }
            }
        }

        viewModelScope.launch {
            connectionManager.output.collect { output ->
                _uiState.update { it.copy(terminalOutput = output) }
            }
        }
    }

    fun selectHost(host: SSHHost) {
        _uiState.update { it.copy(selectedHost = host) }
    }

    fun connect(host: SSHHost) {
        viewModelScope.launch {
            connectionManager.connect(host)
        }
    }

    fun disconnect() {
        connectionManager.disconnect()
    }

    fun sendInput(input: String) {
        viewModelScope.launch {
            connectionManager.sendInput(input)
            _uiState.update { it.copy(terminalOutput = it.terminalOutput + input) }
        }
    }

    fun updateInputText(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun showHostEditor(host: SSHHost? = null) {
        _uiState.update {
            it.copy(
                showHostEditor = true,
                editingHost = host ?: SSHHost(name = "", host = "", username = "")
            )
        }
    }

    fun hideHostEditor() {
        _uiState.update { it.copy(showHostEditor = false, editingHost = null) }
    }

    fun saveHost(host: SSHHost) {
        viewModelScope.launch {
            saveHostUseCase(host)
            hideHostEditor()
        }
    }

    fun deleteHost(host: SSHHost) {
        viewModelScope.launch {
            deleteHostUseCase(host.id)
            if (_uiState.value.selectedHost?.id == host.id) {
                disconnect()
                _uiState.update { it.copy(selectedHost = null) }
            }
        }
    }
}