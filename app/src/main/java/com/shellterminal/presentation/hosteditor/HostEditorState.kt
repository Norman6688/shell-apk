package com.shellterminal.presentation.hosteditor

import com.shellterminal.domain.model.AuthType

data class HostEditorState(
    val id: String = "",
    val name: String = "",
    val host: String = "",
    val port: String = "22",
    val username: String = "",
    val authType: AuthType = AuthType.PASSWORD,
    val password: String = "",
    val privateKey: String = "",
    val passphrase: String = "",
    val isKeyFromFile: Boolean = false,
    val keyFilePath: String = "",
    val isSaving: Boolean = false,
    val error: String? = null
)

sealed class HostEditorEvent {
    data class NameChanged(val value: String) : HostEditorEvent()
    data class HostChanged(val value: String) : HostEditorEvent()
    data class PortChanged(val value: String) : HostEditorEvent()
    data class UsernameChanged(val value: String) : HostEditorEvent()
    data class AuthTypeChanged(val value: AuthType) : HostEditorEvent()
    data class PasswordChanged(val value: String) : HostEditorEvent()
    data class PrivateKeyChanged(val value: String) : HostEditorEvent()
    data class PassphraseChanged(val value: String) : HostEditorEvent()
    data object Save : HostEditorEvent()
    data object Cancel : HostEditorEvent()
}