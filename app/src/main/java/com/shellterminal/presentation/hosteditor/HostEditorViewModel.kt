package com.shellterminal.presentation.hosteditor

import androidx.lifecycle.ViewModel
import com.shellterminal.domain.model.AuthType
import com.shellterminal.domain.model.SSHHost
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class HostEditorViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(HostEditorState())
    val state: StateFlow<HostEditorState> = _state.asStateFlow()

    fun loadHost(host: SSHHost?) {
        if (host == null) {
            _state.value = HostEditorState()
            return
        }
        _state.value = HostEditorState(
            id = host.id,
            name = host.name,
            host = host.host,
            port = host.port.toString(),
            username = host.username,
            authType = host.authType,
            password = host.password ?: "",
            privateKey = host.privateKey ?: "",
            passphrase = host.passphrase ?: ""
        )
    }

    fun onEvent(event: HostEditorEvent) {
        when (event) {
            is HostEditorEvent.NameChanged -> _state.update { it.copy(name = event.value) }
            is HostEditorEvent.HostChanged -> _state.update { it.copy(host = event.value) }
            is HostEditorEvent.PortChanged -> _state.update { it.copy(port = event.value) }
            is HostEditorEvent.UsernameChanged -> _state.update { it.copy(username = event.value) }
            is HostEditorEvent.AuthTypeChanged -> _state.update { it.copy(authType = event.value) }
            is HostEditorEvent.PasswordChanged -> _state.update { it.copy(password = event.value) }
            is HostEditorEvent.PrivateKeyChanged -> _state.update { it.copy(privateKey = event.value) }
            is HostEditorEvent.PassphraseChanged -> _state.update { it.copy(passphrase = event.value) }
            is HostEditorEvent.Save -> save()
            is HostEditorEvent.Cancel -> {}
        }
    }

    fun getHost(): SSHHost? {
        val s = _state.value
        if (s.name.isBlank() || s.host.isBlank() || s.username.isBlank()) {
            _state.update { it.copy(error = "请填写必填项") }
            return null
        }
        val port = s.port.toIntOrNull() ?: 22
        return SSHHost(
            id = s.id.ifEmpty { java.util.UUID.randomUUID().toString() },
            name = s.name,
            host = s.host,
            port = port,
            username = s.username,
            authType = s.authType,
            password = if (s.authType == AuthType.PASSWORD) s.password else null,
            privateKey = if (s.authType == AuthType.PRIVATE_KEY) s.privateKey else null,
            passphrase = if (s.authType == AuthType.PRIVATE_KEY && s.passphrase.isNotEmpty()) s.passphrase else null
        )
    }

    private fun save() {
        // Validation is handled in getHost()
    }
}